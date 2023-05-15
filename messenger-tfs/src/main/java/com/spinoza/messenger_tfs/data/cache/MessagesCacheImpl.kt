package com.spinoza.messenger_tfs.data.cache

import com.spinoza.messenger_tfs.BuildConfig
import com.spinoza.messenger_tfs.data.database.MessengerDao
import com.spinoza.messenger_tfs.data.network.model.event.ReactionEventDto
import com.spinoza.messenger_tfs.data.network.model.message.MessageDto
import com.spinoza.messenger_tfs.data.network.model.message.ReactionDto
import com.spinoza.messenger_tfs.data.utils.dbModelToDto
import com.spinoza.messenger_tfs.data.utils.toDbModel
import com.spinoza.messenger_tfs.data.utils.toDomain
import com.spinoza.messenger_tfs.data.utils.toReactionDto
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesPageType
import com.spinoza.messenger_tfs.domain.model.event.EventOperation
import com.spinoza.messenger_tfs.domain.network.AuthorizationStorage
import com.spinoza.messenger_tfs.domain.util.nameEquals
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.TreeSet
import javax.inject.Inject

class MessagesCacheImpl @Inject constructor(
    private val messengerDao: MessengerDao,
    private val authorizationStorage: AuthorizationStorage,
) : MessagesCache {

    private val data = TreeSet<MessageDto>()
    private val dataMutex = Mutex()

    override fun isNotEmpty(): Boolean {
        return data.isNotEmpty()
    }

    override suspend fun reload() {
        dataMutex.withLock {
            data.clear()
            data.addAll(messengerDao.getMessages().dbModelToDto())
        }
    }

    override suspend fun add(
        messageDto: MessageDto,
        isLastMessageVisible: Boolean,
        filter: MessagesFilter,
    ) {
        dataMutex.withLock {
            data.remove(messageDto)
            data.add(messageDto)
            saveToDatabase(!isLastMessageVisible, filter)
        }
    }

    override suspend fun addAll(
        messagesDto: List<MessageDto>,
        messagesPageType: MessagesPageType,
        filter: MessagesFilter,
    ) {
        dataMutex.withLock {
            if (messagesPageType == MessagesPageType.AFTER_STORED) {
                if (filter.topic.name.isEmpty()) {
                    data.clear()
                } else {
                    data.removeIfSubjectMatchFilter(filter)
                }
            } else {
                messagesDto.forEach { data.remove(it) }
            }
            data.addAll(messagesDto)
            saveToDatabase(messagesPageType == MessagesPageType.OLDEST, filter)
        }
    }

    override suspend fun update(messageId: Long, subject: String?, content: String?) {
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

    override suspend fun remove(messageId: Long) {
        dataMutex.withLock {
            data.removeIf { it.id == messageId }
        }
    }

    override suspend fun getFirstMessageId(filter: MessagesFilter): Long {
        val messages = getFilteredMessages(filter)
        return if (messages.isEmpty()) Message.UNDEFINED_ID else messages.first().id
    }

    override suspend fun getLastMessageId(filter: MessagesFilter): Long {
        val messages = getFilteredMessages(filter)
        return if (messages.isEmpty()) Message.UNDEFINED_ID else messages.last().id
    }

    override suspend fun updateReaction(messageId: Long, userId: Long, reactionDto: ReactionDto) {
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

    override suspend fun updateReaction(reactionEventDto: ReactionEventDto) {
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

    override suspend fun getMessages(filter: MessagesFilter): List<Message> {
        return getFilteredMessages(filter).toDomain(authorizationStorage.getUserId())
    }

    private suspend fun getFilteredMessages(filter: MessagesFilter): Collection<MessageDto> {
        dataMutex.withLock {
            val streamMessages =
                if (filter.channel.channelId != Channel.UNDEFINED_ID) {
                    data.filter { filter.channel.channelId == it.streamId }
                } else {
                    data
                }
            val topicMessages =
                if (filter.topic.name.isNotEmpty()) {
                    streamMessages.filter { filter.topic.nameEquals(it.subject) }
                } else {
                    streamMessages
                }
            return topicMessages
        }
    }

    private fun replace(messageDto: MessageDto) {
        data.remove(messageDto)
        data.add(messageDto)
    }

    private suspend fun saveToDatabase(
        isReducingFromTail: Boolean,
        filter: MessagesFilter,
    ) {
        if (data.size > MAX_CACHE_SIZE && filter.topic.name.isNotEmpty()) {
            data.removeIfSubjectNotMatchFilter(filter)
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

    private fun TreeSet<MessageDto>.removeIfSubjectMatchFilter(filter: MessagesFilter) {
        this.removeIf { filter.topic.nameEquals(it.subject) }
    }

    private fun TreeSet<MessageDto>.removeIfSubjectNotMatchFilter(filter: MessagesFilter) {
        this.removeIf { !filter.topic.nameEquals(it.subject) }
    }

    private companion object {

        private const val UNDEFINED_EVENT_ID = -1L
        private const val MAX_CACHE_SIZE = BuildConfig.MAX_MESSAGES_CACHE_SIZE
    }
}