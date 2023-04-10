package com.cyberfox21.tinkofffintechseminar.di

import com.spinoza.messenger_tfs.App
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.domain.usecase.*


class GlobalDI private constructor() {

    private val repository by lazy { MessagesRepositoryImpl.getInstance() }

    val globalRouter by lazy { App.router }

    val deleteEventQueueUseCase by lazy { DeleteEventQueueUseCase(repository) }
    val getChannelsEventsUseCase by lazy { GetChannelEventsUseCase(repository) }
    val getChannelsUseCase by lazy { GetChannelsUseCase(repository) }
    val getDeleteMessageEventUseCase by lazy { GetDeleteMessageEventUseCase(repository) }
    val getMessagesUseCase by lazy { GetMessagesUseCase(repository) }
    val getOwnUserUseCase by lazy { GetOwnUserUseCase(repository) }
    val getPresenceEventsUseCase by lazy { GetPresenceEventsUseCase(repository) }
    val getReactionEventUseCase by lazy { GetReactionEventUseCase(repository) }
    val getTopicsUseCase by lazy { GetTopicsUseCase(repository) }
    val getTopicUseCase by lazy { GetTopicUseCase(repository) }
    val getUsersByFilterUseCase by lazy { GetUsersByFilterUseCase(repository) }
    val getUserUseCase by lazy { GetUserUseCase(repository) }
    val registerEventQueueUseCase by lazy { RegisterEventQueueUseCase(repository) }
    val sendMessageUseCase by lazy { SendMessageUseCase(repository) }
    val setMessagesFlagToReadUserCase by lazy { SetMessagesFlagToReadUserCase(repository) }
    val setOwnStatusActiveUseCase by lazy { SetOwnStatusActiveUseCase(repository) }
    val updateReactionUseCase by lazy { UpdateReactionUseCase(repository) }

    companion object {

        lateinit var INSTANCE: GlobalDI

        fun init() {
            INSTANCE = GlobalDI()
        }
    }
}
