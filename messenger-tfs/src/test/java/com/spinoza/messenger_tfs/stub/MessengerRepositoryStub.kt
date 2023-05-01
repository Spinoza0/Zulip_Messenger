package com.spinoza.messenger_tfs.stub

import com.spinoza.messenger_tfs.data.network.model.stream.StreamDto
import com.spinoza.messenger_tfs.data.network.model.user.UserDto
import com.spinoza.messenger_tfs.data.utils.dtoToDomain
import com.spinoza.messenger_tfs.data.utils.toDomain
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesPageType
import com.spinoza.messenger_tfs.domain.model.MessagesResult
import com.spinoza.messenger_tfs.domain.model.RepositoryError
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.model.event.ChannelEvent
import com.spinoza.messenger_tfs.domain.model.event.DeleteMessageEvent
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.model.event.MessageEvent
import com.spinoza.messenger_tfs.domain.model.event.PresenceEvent
import com.spinoza.messenger_tfs.domain.model.event.ReactionEvent
import com.spinoza.messenger_tfs.domain.repository.MessengerRepository

class MessengerRepositoryStub : MessengerRepository {

    private val ownUser = createUserDto(0)
    private val user = createUserDto(1)

    override suspend fun getApiKey(
        storedApiKey: String,
        email: String,
        password: String,
    ): Result<String> {
        return Result.success("API key")
    }

    override suspend fun getOwnUserId(): Result<Long> {
        return Result.success(ownUser.userId)
    }

    override suspend fun getOwnUser(): Result<User> {
        return Result.success(ownUser.toDomain(provideUserPresence()))
    }

    override suspend fun getUser(userId: Long): Result<User> {
        return Result.success(user.toDomain(provideUserPresence()))
    }

    override suspend fun getAllUsers(): Result<List<User>> {
        val presence = provideUserPresence()
        val users = listOf(ownUser.toDomain(presence), user.toDomain(presence))
        return Result.success(users)
    }

    override suspend fun getStoredMessages(filter: MessagesFilter): Result<MessagesResult> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun getMessages(
        messagesPageType: MessagesPageType,
        filter: MessagesFilter,
    ): Result<MessagesResult> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun getStoredChannels(channelsFilter: ChannelsFilter): Result<List<Channel>> {
        return createChannels(channelsFilter)
    }

    override suspend fun getChannels(channelsFilter: ChannelsFilter): Result<List<Channel>> {
        return createChannels(channelsFilter)
    }

    override suspend fun getStoredTopics(channel: Channel): Result<List<Topic>> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun getTopics(channel: Channel): Result<List<Topic>> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun getTopic(filter: MessagesFilter): Result<Topic> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun getUpdatedMessageFilter(filter: MessagesFilter): MessagesFilter {
        return MessagesFilter()
    }

    override suspend fun sendMessage(content: String, filter: MessagesFilter): Result<Long> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun updateReaction(
        messageId: Long,
        emoji: Emoji,
        filter: MessagesFilter,
    ): Result<MessagesResult> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun registerEventQueue(
        eventTypes: List<EventType>,
        messagesFilter: MessagesFilter,
    ): Result<EventsQueue> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun deleteEventQueue(queueId: String) {}

    override suspend fun getPresenceEvents(queue: EventsQueue): Result<List<PresenceEvent>> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun getChannelEvents(
        queue: EventsQueue,
        channelsFilter: ChannelsFilter,
    ): Result<List<ChannelEvent>> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun getMessageEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<MessageEvent> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun getDeleteMessageEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<DeleteMessageEvent> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun getReactionEvent(
        queue: EventsQueue,
        filter: MessagesFilter,
        isLastMessageVisible: Boolean,
    ): Result<ReactionEvent> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun setOwnStatusActive() {}

    override suspend fun setMessagesFlagToRead(messageIds: List<Long>) {}

    private fun createUserDto(id: Long): UserDto {
        return UserDto(userId = id)
    }

    private fun provideUserPresence(): User.Presence {
        return User.Presence.ACTIVE
    }

    private fun createChannels(channelsFilter: ChannelsFilter): Result<List<Channel>> {
        var id = 0L
        val streams = mutableListOf<StreamDto>()
        repeat(5) {
            streams.add(createStream(id++))
        }
        return Result.success(streams.dtoToDomain(channelsFilter))
    }

    private fun createStream(streamId: Long): StreamDto {
        return StreamDto(
            streamId = streamId,
            name = "Name $streamId",
            dateCreated = 0L,
            firstMessageId = null,
            inviteOnly = false,
            isAnnouncementOnly = false
        )
    }

    private companion object {

        const val ERROR_MSG = "Repository error"
    }
}