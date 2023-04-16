package com.spinoza.messenger_tfs.di

import android.content.Context
import com.spinoza.messenger_tfs.presentation.feature.app.App
import com.spinoza.messenger_tfs.data.network.ZulipApiFactory
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.feature.login.model.LoginScreenState
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenState
import com.spinoza.messenger_tfs.presentation.feature.people.model.PeopleScreenState
import com.spinoza.messenger_tfs.presentation.feature.profile.model.ProfileScreenState
import com.spinoza.messenger_tfs.presentation.feature.login.LoginStorageImpl
import com.spinoza.messenger_tfs.presentation.feature.login.LoginActor
import com.spinoza.messenger_tfs.presentation.feature.login.LoginReducer
import com.spinoza.messenger_tfs.presentation.feature.messages.MessagesActor
import com.spinoza.messenger_tfs.presentation.feature.messages.MessagesReducer
import com.spinoza.messenger_tfs.presentation.feature.people.PeopleActor
import com.spinoza.messenger_tfs.presentation.feature.people.PeopleReducer
import com.spinoza.messenger_tfs.presentation.feature.profile.ProfileActor
import com.spinoza.messenger_tfs.presentation.feature.profile.ProfileReducer
import vivid.money.elmslie.coroutines.ElmStoreCompat


class GlobalDI private constructor(context: Context) {

    private val repository by lazy { MessagesRepositoryImpl.getInstance() }

    val globalRouter by lazy { App.router }
    val globalNavigatorHolder by lazy { App.navigatorHolder }
    val apiService = ZulipApiFactory.apiService

    val checkLoginUseCase by lazy { CheckLoginUseCase(repository) }
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