package com.spinoza.messenger_tfs.data.cache

import com.spinoza.messenger_tfs.data.network.model.event.ReactionEventDto
import com.spinoza.messenger_tfs.data.network.model.message.MessageDto
import com.spinoza.messenger_tfs.data.network.model.message.ReactionDto
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesPageType

interface MessagesCache {

    fun isNotEmpty(): Boolean

    suspend fun reload()

    suspend fun add(messageDto: MessageDto, isLastMessageVisible: Boolean, filter: MessagesFilter)

    suspend fun addAll(
        messagesDto: List<MessageDto>,
        messagesPageType: MessagesPageType,
        filter: MessagesFilter,
    )

    suspend fun update(messageId: Long, subject: String?, content: String?)

    suspend fun remove(messageId: Long)

    suspend fun getFirstMessageId(filter: MessagesFilter): Long

    suspend fun getLastMessageId(filter: MessagesFilter): Long

    suspend fun updateReaction(messageId: Long, userId: Long, reactionDto: ReactionDto)

    suspend fun updateReaction(reactionEventDto: ReactionEventDto)

    suspend fun getMessages(filter: MessagesFilter): List<Message>
}