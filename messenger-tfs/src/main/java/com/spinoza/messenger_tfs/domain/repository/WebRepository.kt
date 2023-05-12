package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesPageType
import com.spinoza.messenger_tfs.domain.model.MessagesResult
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.model.User

interface WebRepository {

    suspend fun logIn(email: String, password: String): Result<Boolean>

    suspend fun getOwnUser(): Result<User>

    suspend fun getUser(userId: Long): Result<User>

    suspend fun getAllUsers(): Result<List<User>>

    suspend fun getMessages(
        messagesPageType: MessagesPageType,
        filter: MessagesFilter,
    ): Result<MessagesResult>

    suspend fun getMessageRawContent(messageId: Long, default: String): String

    suspend fun editMessage(messageId: Long, topic: String, content: String): Result<Long>

    suspend fun deleteMessage(messageId: Long): Result<Boolean>

    suspend fun getChannelSubscriptionStatus(channelId: Long): Result<Boolean>

    suspend fun createChannel(name: String, description: String): Result<Boolean>

    suspend fun unsubscribeFromChannel(name: String): Result<Boolean>

    suspend fun deleteChannel(channelId: Long): Result<Boolean>

    suspend fun getChannels(channelsFilter: ChannelsFilter): Result<List<Channel>>

    suspend fun getTopics(channel: Channel): Result<List<Topic>>

    suspend fun getTopic(filter: MessagesFilter): Result<Topic>

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

    suspend fun setOwnStatusActive()

    suspend fun setMessagesFlagToRead(messageIds: List<Long>)
}