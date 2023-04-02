package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagesFilter

sealed class RepositoryResult<out T> {

    class Success<T>(val value: T) : RepositoryResult<T>()

    sealed class Failure : RepositoryResult<Nothing>() {

        class Network(val value: String) : Failure()

        class RegisterPresenceEventQueue(val value: String) : Failure()

        class GetEvents(val value: String) : Failure()

        class OwnUserNotFound(val value: String) : Failure()

        class UserNotFound(val userId: Long, val value: String) : Failure()

        class MessageNotFound(val messageId: Long) : Failure()

        class LoadingUsers(val value: String) : Failure()

        class LoadingMessages(val messagesFilter: MessagesFilter, val value: String) : Failure()

        class LoadingChannels(val channelsFilter: ChannelsFilter, val value: String) : Failure()

        class LoadingTopicData(val messagesFilter: MessagesFilter) : Failure()

        class LoadingChannelTopics(val channel: Channel, val value: String) : Failure()

        class SendingMessage(val value: String) : Failure()

        class UpdatingReaction(val value: String) : Failure()
    }
}