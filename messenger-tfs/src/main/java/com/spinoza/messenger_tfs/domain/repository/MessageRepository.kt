package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesPageType
import com.spinoza.messenger_tfs.domain.model.MessagesResult

interface MessageRepository {

    suspend fun getMessages(
        messagesPageType: MessagesPageType,
        filter: MessagesFilter,
    ): Result<MessagesResult>

    suspend fun getMessageRawContent(messageId: Long, default: String): String

    suspend fun editMessage(messageId: Long, topic: String, content: String): Result<Long>

    suspend fun deleteMessage(messageId: Long): Result<Boolean>

    suspend fun getUpdatedMessageFilter(filter: MessagesFilter): MessagesFilter

    suspend fun sendMessage(
        subject: String,
        content: String,
        filter: MessagesFilter,
    ): Result<Long>

    suspend fun updateReaction(
        messageId: Long,
        emoji: Emoji,
        filter: MessagesFilter,
    ): Result<MessagesResult>

    suspend fun setMessagesFlagToRead(messageIds: List<Long>)
}