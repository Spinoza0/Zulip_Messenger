package com.spinoza.messenger_tfs.di.messages

import androidx.lifecycle.Lifecycle
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.feature.messages.MessagesActor
import com.spinoza.messenger_tfs.presentation.feature.messages.MessagesReducer
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenEffect
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenState
import dagger.Module
import dagger.Provides
import vivid.money.elmslie.coroutines.ElmStoreCompat

@Module
class MessagesModule {

    @Provides
    fun provideMessagesScreenState(): MessagesScreenState = MessagesScreenState()

    @Provides
    fun provideMessagesActor(
        lifecycle: Lifecycle,
        getOwnUserIdUseCase: GetOwnUserIdUseCase,
        getMessagesUseCase: GetMessagesUseCase,
        sendMessageUseCase: SendMessageUseCase,
        updateReactionUseCase: UpdateReactionUseCase,
        getMessageEventUseCase: GetMessageEventUseCase,
        getDeleteMessageEventUseCase: GetDeleteMessageEventUseCase,
        getReactionEventUseCase: GetReactionEventUseCase,
        setOwnStatusActiveUseCase: SetOwnStatusActiveUseCase,
        setMessagesFlagToReadUserCase: SetMessagesFlagToReadUserCase,
        registerEventQueueUseCase: RegisterEventQueueUseCase,
        deleteEventQueueUseCase: DeleteEventQueueUseCase,
    ): MessagesActor = MessagesActor(
        lifecycle,
        getOwnUserIdUseCase,
        getMessagesUseCase,
        sendMessageUseCase,
        updateReactionUseCase,
        getMessageEventUseCase,
        getDeleteMessageEventUseCase,
        getReactionEventUseCase,
        setOwnStatusActiveUseCase,
        setMessagesFlagToReadUserCase,
        registerEventQueueUseCase,
        deleteEventQueueUseCase,
    )

    @Provides
    fun provideMessagesReducer(router: Router): MessagesReducer = MessagesReducer(router)

    @Provides
    fun provideMessagesStore(
        state: MessagesScreenState,
        actor: MessagesActor,
        reducer: MessagesReducer,
    ): ElmStoreCompat<
            MessagesScreenEvent,
            MessagesScreenState,
            MessagesScreenEffect,
            MessagesScreenCommand> =
        ElmStoreCompat(state, reducer, actor)
}