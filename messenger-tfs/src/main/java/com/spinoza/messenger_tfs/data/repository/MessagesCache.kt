package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.model.event.ReactionEventDto
import com.spinoza.messenger_tfs.data.model.message.MessageDto
import com.spinoza.messenger_tfs.data.model.message.ReactionDto
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import java.util.*

class MessagesCache {

    private val data = TreeSet<MessageDto>()

    fun add(messageDto: MessageDto) {
        data.add(messageDto)
    }

    fun replaceAll(messagesDto: List<MessageDto>) {
        data.clear()
        data.addAll(messagesDto)
    }

    fun remove(messageId: Long) {
        data.removeIf { it.id == messageId }
    }

    fun updateReaction(reactionEventDto: ReactionEventDto) {
        data.find { it.id == reactionEventDto.messageId }?.let { messageDto ->
            if (reactionEventDto.operation == ReactionEventDto.Operation.ADD.value) {
                val reactions = mutableListOf<ReactionDto>()
                reactions.addAll(messageDto.reactions)
                reactions.add(reactionEventDto.toReactionDto())
                val newMessageDto = messageDto.copy(reactions = reactions)
                data.remove(messageDto)
                data.add(newMessageDto)
            }
            if (reactionEventDto.operation == ReactionEventDto.Operation.REMOVE.value) {
                val reactions = mutableListOf<ReactionDto>()
                val reactionToRemove = reactionEventDto.toReactionDto()
                reactions.addAll(messageDto.reactions.filter { it != reactionToRemove })
                val newMessageDto = messageDto.copy(reactions = reactions)
                data.remove(messageDto)
                data.add(newMessageDto)
            }
        }
    }

    fun getMessages(messagesFilter: MessagesFilter): List<MessageDto> {
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
            }
    }

    private fun ReactionEventDto.toReactionDto(): ReactionDto {
        return ReactionDto(
            emoji_name,
            emoji_code,
            reaction_type,
            userId
        )
    }
}