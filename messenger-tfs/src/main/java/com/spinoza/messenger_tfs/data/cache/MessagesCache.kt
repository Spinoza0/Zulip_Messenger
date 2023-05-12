package com.spinoza.messenger_tfs.data.cache

import com.spinoza.messenger_tfs.BuildConfig
import com.spinoza.messenger_tfs.data.database.MessengerDao
import com.spinoza.messenger_tfs.domain.model.event.EventOperation
import com.spinoza.messenger_tfs.data.network.model.event.ReactionEventDto
import com.spinoza.messenger_tfs.data.network.model.message.MessageDto
import com.spinoza.messenger_tfs.data.network.model.message.ReactionDto
import com.spinoza.messenger_tfs.data.utils.dbModelToDto
import com.spinoza.messenger_tfs.data.utils.isEqualTopicName
import com.spinoza.messenger_tfs.data.utils.toDbModel
import com.spinoza.messenger_tfs.data.utils.toDomain
import com.spinoza.messenger_tfs.data.utils.toReactionDto
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesPageType
import com.spinoza.messenger_tfs.domain.network.AuthorizationStorage
import com.spinoza.messenger_tfs.domain.util.EMPTY_STRING
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.TreeSet
import javax.inject.Inject

class MessagesCache @Inject constructor(
    private val messengerDao: MessengerDao,
    private val authorizationStorage: AuthorizationStorage,
) {

    private val data = TreeSet<MessageDto>()
    private val dataMutex = Mutex()

    fun isNotEmpty(): Boolean {
        return data.isNotEmpty()
    }

    suspend fun reload() {
        dataMutex.withLock {
            data.clear()
            data.addAll(messengerDao.getMessages().dbModelToDto())
        }
    }

    suspend fun add(messageDto: MessageDto, isLastMessageVisible: Boolean) {
        dataMutex.withLock {
            data.remove(messageDto)
            data.add(messageDto)
            saveToDatabase(!isLastMessageVisible, messageDto.subject)
        }
    }

    suspend fun addAll(messagesDto: List<MessageDto>, messagesPageType: MessagesPageType) {
        dataMutex.withLock {
            messagesDto.forEach { data.remove(it) }
            data.addAll(messagesDto)
            if (data.isNotEmpty()) {
                val subject = if (messagesDto.isNotEmpty()) {
                    messagesDto.first().subject
                } else {
                    EMPTY_STRING
                }
                saveToDatabase(messagesPageType == MessagesPageType.OLDEST, subject)
            }
        }
    }

    suspend fun update(messageId: Long, subject: String?, content: String?) {
        dataMutex.withLock {
            val oldMessage = data.find { it.id == messageId } ?: return
            var newMessage = oldMessage
            if (subject != null) {
                newMessage = oldMessage.copy(subject = subject)
            }
            if (content != null) {
                newMessage = newMessage.copy(content = content)
            }
            replace(newMessage)
        }
    }

    suspend fun remove(messageId: Long) {
        dataMutex.withLock {
            data.removeIf { it.id == messageId }
        }
    }

    fun getFirstMessageId(filter: MessagesFilter): Long {
        val message = data.find { filter.isEqualTopicName(it.subject) }
        return message?.id ?: Message.UNDEFINED_ID
    }

    fun getLastMessageId(filter: MessagesFilter): Long {
        val message = data.findLast { filter.isEqualTopicName(it.subject) }
        return message?.id ?: Message.UNDEFINED_ID
    }

    suspend fun updateReaction(messageId: Long, userId: Long, reactionDto: ReactionDto) {
        val message = data.find { messageId == it.id } ?: return
        val isAddReaction = null == message.reactions.find {
            it.emojiName == reactionDto.emojiName && it.userId == userId
        }
        updateReaction(
            ReactionEventDto(
                UNDEFINED_EVENT_ID,
                if (isAddReaction) EventOperation.ADD.value else EventOperation.REMOVE.value,
                reactionDto.userId,
                messageId,
                reactionDto.emojiName,
                reactionDto.emojiCode,
                reactionDto.reactionType
            )
        )
    }

    suspend fun updateReaction(reactionEventDto: ReactionEventDto) {
        dataMutex.withLock {
            val message = data.find { it.id == reactionEventDto.messageId } ?: return
            val isUserReactionExisting = message.reactions.find {
                it.emojiName == reactionEventDto.emoji_name && it.userId == reactionEventDto.userId
            } != null
            if (reactionEventDto.operation == EventOperation.ADD.value &&
                !isUserReactionExisting
            ) {
                val reactions = mutableListOf<ReactionDto>()
                reactions.addAll(message.reactions)
                reactions.add(reactionEventDto.toReactionDto())
                replace(message.copy(reactions = reactions))
            }
            if (reactionEventDto.operation == EventOperation.REMOVE.value && isUserReactionExisting) {
                val reactions = mutableListOf<ReactionDto>()
                val reactionToRemove = reactionEventDto.toReactionDto()
                reactions.addAll(message.reactions.filter { it != reactionToRemove })
                replace(message.copy(reactions = reactions))
            }
        }
    }

    suspend fun getMessages(filter: MessagesFilter): List<Message> {
        dataMutex.withLock {
            val streamMessages =
                if (filter.channel.channelId != Channel.UNDEFINED_ID) {
                    data.filter { filter.channel.channelId == it.streamId }
                } else {
                    data
                }
            val topicMessages =
                if (filter.topic.name.isNotEmpty()) {
                    streamMessages.filter { filter.isEqualTopicName(it.subject) }
                } else {
                    streamMessages
                }
            return topicMessages.toDomain(authorizationStorage.getUserId())
        }
    }

    private fun replace(messageDto: MessageDto) {
        data.remove(messageDto)
        data.add(messageDto)
    }

    private suspend fun saveToDatabase(isReducingFromTail: Boolean, subject: String) {
        if (data.size > MAX_CACHE_SIZE && subject.isNotEmpty()) {
            data.removeIf { it.subject != subject }
        }
        if (data.size > MAX_CACHE_SIZE) {
            val delta = data.size - MAX_CACHE_SIZE
            if (isReducingFromTail) {
                data.tailSet(data.elementAt(data.size - delta), true).clear()
            } else {
                data.headSet(data.elementAt(delta), true).clear()
            }
        }
        messengerDao.removeMessages()
        messengerDao.insertMessages(data.toDbModel())
    }

    private companion object {

        private const val UNDEFINED_EVENT_ID = -1L
        private const val MAX_CACHE_SIZE = BuildConfig.MAX_MESSAGES_CACHE_SIZE
    }
}