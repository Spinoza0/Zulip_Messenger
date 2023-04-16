package com.spinoza.messenger_tfs.di

import com.spinoza.messenger_tfs.data.network.ZulipApiFactory
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.feature.app.App
import com.spinoza.messenger_tfs.presentation.feature.messages.MessagesActor
import com.spinoza.messenger_tfs.presentation.feature.messages.MessagesReducer
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenState
import com.spinoza.messenger_tfs.presentation.feature.people.PeopleActor
import com.spinoza.messenger_tfs.presentation.feature.people.PeopleReducer
import com.spinoza.messenger_tfs.presentation.feature.people.model.PeopleScreenState
import kotlinx.serialization.json.Json
import vivid.money.elmslie.coroutines.ElmStoreCompat


class GlobalDI private constructor() {

    private val repository by lazy {
        MessagesRepositoryImpl.getInstance(ZulipApiFactory.apiService, Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        })
    }

    val globalRouter by lazy { App.router }

    val getDeleteMessageEventUseCase by lazy { GetDeleteMessageEventUseCase(repository) }
    val getMessageEventUseCase by lazy { GetMessageEventUseCase(repository) }
    val getMessagesUseCase by lazy { GetMessagesUseCase(repository) }
    val getOwnUserIdUseCase by lazy { GetOwnUserIdUseCase(repository) }
    val getPresenceEventsUseCase by lazy { GetPresenceEventsUseCase(repository) }
    val getReactionEventUseCase by lazy { GetReactionEventUseCase(repository) }
    val getUsersByFilterUseCase by lazy { GetUsersByFilterUseCase(repository) }
    val sendMessageUseCase by lazy { SendMessageUseCase(repository) }
    val setMessagesFlagToReadUserCase by lazy { SetMessagesFlagToReadUserCase(repository) }
    val setOwnStatusActiveUseCase by lazy { SetOwnStatusActiveUseCase(repository) }
    val updateReactionUseCase by lazy { UpdateReactionUseCase(repository) }
    val registerEventQueueUseCase by lazy { RegisterEventQueueUseCase(repository) }
    val deleteEventQueueUseCase by lazy { DeleteEventQueueUseCase(repository) }

    fun providePeopleStore(actor: PeopleActor) = ElmStoreCompat(
        initialState = PeopleScreenState(),
        reducer = PeopleReducer(),
        actor = actor
    )

    fun provideMessagesStore(actor: MessagesActor) = ElmStoreCompat(
        initialState = MessagesScreenState(),
        reducer = MessagesReducer(),
        actor = actor
    )

    companion object {

        lateinit var INSTANCE: GlobalDI

        fun init() {
            INSTANCE = GlobalDI()
        }
    }
}