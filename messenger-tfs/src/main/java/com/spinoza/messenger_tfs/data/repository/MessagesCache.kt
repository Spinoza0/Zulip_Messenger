package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.network.model.event.ReactionEventDto
import com.spinoza.messenger_tfs.data.network.model.message.MessageDto
import com.spinoza.messenger_tfs.data.network.model.message.ReactionDto
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagesAnchor
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import java.util.*
import javax.inject.Inject

class MessagesCache @Inject constructor() {

    private val data = TreeSet<MessageDto>()
    private val lock = Any()

    fun isNotEmpty(): Boolean {
        return data.isNotEmpty()
    }

    fun add(messageDto: MessageDto, isLastMessageVisible: Boolean) {
        synchronized(lock) {
            data.remove(messageDto)
            data.add(messageDto)
        }
    }

    fun addAll(messagesDto: List<MessageDto>, anchor: MessagesAnchor) {
        synchronized(lock) {
            messagesDto.forEach { data.remove(it) }
            data.addAll(messagesDto)
        }
    }

    fun remove(messageId: Long) {
        synchronized(lock) {
            data.removeIf { it.id == messageId }
        }
    }

    fun firstMessageId(): Long {
        return if (data.isNotEmpty()) data.first().id else Message.UNDEFINED_ID
    }

    fun lastMessageId(): Long {
        return if (data.isNotEmpty()) data.last().id else Message.UNDEFINED_ID
    }

    fun updateReaction(messageId: Long, userId: Long, reactionDto: ReactionDto) {
        val messages = data.filter { messageId == it.id }
        if (messages.isEmpty()) return
        val isAddReaction = null == messages.first().reactions.find {
            it.emojiName == reactionDto.emojiName && it.userId == userId
        }
        updateReaction(
            ReactionEventDto(
                UNDEFINED_EVENT_ID,
                if (isAddReaction) ReactionEventDto.Operation.ADD.value
                else ReactionEventDto.Operation.REMOVE.value,
                reactionDto.userId,
                messageId,
                reactionDto.emojiName,
                reactionDto.emojiCode,
                reactionDto.reactionType
            )
        )
    }

    fun updateReaction(reactionEventDto: ReactionEventDto) {
        synchronized(lock) {
            data.find { it.id == reactionEventDto.messageId }?.let { messageDto ->
                val isUserReactionExisting = messageDto.reactions.find {
                    it.emojiName == reactionEventDto.emoji_name &&
                            it.userId == reactionEventDto.userId
                } != null
                if (reactionEventDto.operation == ReactionEventDto.Operation.ADD.value &&
                    !isUserReactionExisting
                ) {
                    val reactions = mutableListOf<ReactionDto>()
                    reactions.addAll(messageDto.reactions)
                    reactions.add(reactionEventDto.toReactionDto())
                    replace(messageDto.copy(reactions = reactions))
                }
                if (reactionEventDto.operation == ReactionEventDto.Operation.REMOVE.value &&
                    isUserReactionExisting
                ) {
                    val reactions = mutableListOf<ReactionDto>()
                    val reactionToRemove = reactionEventDto.toReactionDto()
                    reactions.addAll(messageDto.reactions.filter { it != reactionToRemove })
                    replace(messageDto.copy(reactions = reactions))
                }
            }
        }
    }

    fun getMessages(
        filter: MessagesFilter,
        isUseAnchor: Boolean = false,
        anchor: Long = Message.UNDEFINED_ID,
    ): List<MessageDto> {
        synchronized(lock) {
            val streamMessages =
                if (filter.channel.channelId != Channel.UNDEFINED_ID) {
                    data.filter { filter.channel.channelId == it.streamId }
                } else {
                    data
                }
            val topicMessages =
                if (filter.topic.name.isNotEmpty()) {
                    streamMessages.filter { filter.topic.name.equals(it.subject, true) }
                } else {
                    streamMessages
                }
            return if (isUseAnchor) {
                if (anchor != Message.UNDEFINED_ID)
                    topicMessages.filter { it.id >= anchor }.toList()
                else
                    emptyList()
            } else
                topicMessages.toList()
        }
    }

    private fun replace(messageDto: MessageDto) {
        synchronized(lock) {
            data.remove(messageDto)
            data.add(messageDto)
        }
    }

    private companion object {

        private const val UNDEFINED_EVENT_ID = -1L
        private const val MAX_CACHE_SIZE = 50
    }
}