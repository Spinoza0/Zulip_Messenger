package com.spinoza.messenger_tfs.di

import android.content.Context
import com.spinoza.messenger_tfs.App
import com.spinoza.messenger_tfs.data.network.ZulipApiFactory
import com.spinoza.messenger_tfs.data.network.ZulipAuthKeeper
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.elmstore.*
import com.spinoza.messenger_tfs.presentation.model.login.LoginScreenState
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesScreenState
import com.spinoza.messenger_tfs.presentation.model.people.PeopleScreenState
import com.spinoza.messenger_tfs.presentation.model.profile.ProfileScreenState
import com.spinoza.messenger_tfs.presentation.elmstore.LoginStorageImpl
import vivid.money.elmslie.coroutines.ElmStoreCompat


class GlobalDI private constructor(context: Context) {

    private val repository by lazy { MessagesRepositoryImpl.getInstance(ZulipAuthKeeper) }

    val globalRouter by lazy { App.router }
    val globalNavigatorHolder by lazy { App.navigatorHolder }
    val apiFactory = ZulipApiFactory

    val getApiKeyUseCase by lazy { GetApiKeyUseCase(repository) }
    val deleteEventQueueUseCase by lazy { DeleteEventQueueUseCase(repository) }
    val getChannelEventsUseCase by lazy { GetChannelEventsUseCase(repository) }
    val getChannelsUseCase by lazy { GetChannelsUseCase(repository) }
    val getDeleteMessageEventUseCase by lazy { GetDeleteMessageEventUseCase(repository) }
    val getMessageEventUseCase by lazy { GetMessageEventUseCase(repository) }
    val getMessagesUseCase by lazy { GetMessagesUseCase(repository) }
    val getOwnUserIdUseCase by lazy { GetOwnUserIdUseCase(repository) }
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

    private val loginStorage by lazy { LoginStorageImpl(context) }

    fun getChannelsFilter(isAllChannels: Boolean) =
        ChannelsFilter(ChannelsFilter.NO_FILTER, !isAllChannels)

    fun provideLoginStore(actor: LoginActor) = ElmStoreCompat(
        initialState = LoginScreenState(),
        reducer = LoginReducer(loginStorage),
        actor = actor
    )

    fun provideProfileStore(initialState: ProfileScreenState, actor: ProfileActor) = ElmStoreCompat(
        initialState = initialState,
        reducer = ProfileReducer(),
        actor = actor
    )

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

        fun init(context: Context) {
            INSTANCE = GlobalDI(context)
        }
    }
}