package com.spinoza.messenger_tfs.data

import com.spinoza.messenger_tfs.data.model.event.ReactionEventDto
import com.spinoza.messenger_tfs.data.model.message.MessageDto
import com.spinoza.messenger_tfs.data.model.message.ReactionDto
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import java.util.*

class MessagesCache {

    private val data = TreeSet<MessageDto>()
    private val LOCK = Any()

    fun add(messageDto: MessageDto) {
        synchronized(LOCK) {
            data.remove(messageDto)
            data.add(messageDto)
        }
    }

    fun replaceAll(messagesDto: List<MessageDto>) {
        synchronized(LOCK) {
            messagesDto.forEach { data.remove(it) }
            data.addAll(messagesDto)
        }
    }

    fun remove(messageId: Long) {
        synchronized(LOCK) {
            data.removeIf { it.id == messageId }
        }
    }

    fun updateReaction(messageId: Long, reactionDto: ReactionDto, isAddReaction: Boolean) {
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
        synchronized(LOCK) {
            data.find { it.id == reactionEventDto.messageId }?.let { messageDto ->
                val isUserReactionExists = messageDto.reactions.find {
                    it.emojiName == reactionEventDto.emoji_name &&
                            it.userId == reactionEventDto.userId
                } != null
                if (reactionEventDto.operation == ReactionEventDto.Operation.ADD.value &&
                    !isUserReactionExists
                ) {
                    val reactions = mutableListOf<ReactionDto>()
                    reactions.addAll(messageDto.reactions)
                    reactions.add(reactionEventDto.toReactionDto())
                    add(messageDto.copy(reactions = reactions))
                }
                if (reactionEventDto.operation == ReactionEventDto.Operation.REMOVE.value &&
                    isUserReactionExists
                ) {
                    val reactions = mutableListOf<ReactionDto>()
                    val reactionToRemove = reactionEventDto.toReactionDto()
                    reactions.addAll(messageDto.reactions.filter { it != reactionToRemove })
                    add(messageDto.copy(reactions = reactions))
                }
            }
        }
    }

    fun getMessages(messagesFilter: MessagesFilter): List<MessageDto> {
        synchronized(LOCK) {
            return data
                .filter {
                    if (messagesFilter.channel.channelId != Channel.UNDEFINED_ID)
                        messagesFilter.channel.channelId == it.streamId
                    else true
                }
                .filter {
                    if (messagesFilter.topic.name.isNotEmpty())
                        messagesFilter.topic.name == it.subject
                    else true
                }.toList()
        }
    }

    private companion object {

        private const val UNDEFINED_EVENT_ID = -1L
    }
}