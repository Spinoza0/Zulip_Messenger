package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagesFilter

sealed class RepositoryResult<out T> {

    class Success<T>(val value: T) : RepositoryResult<T>()

    sealed class Failure : RepositoryResult<Nothing>() {

        class CurrentUserNotFound(val value: String) : Failure()

        class UserNotFound(val userId: Long) : Failure()

        class MessageNotFound(val messageId: Long) : Failure()

        class LoadingUsers(val value: String) : Failure()

        class LoadingMessages(val messagesFilter: MessagesFilter) : Failure()

        class LoadingChannels(val channelsFilter: ChannelsFilter) : Failure()

        class LoadingTopicData(val messagesFilter: MessagesFilter) : Failure()

        class LoadingChannelTopics(val channel: Channel) : Failure()

        class SendingMessage(val value: String) : Failure()

        class UpdatingReaction(val value: String) : Failure()
    }
}