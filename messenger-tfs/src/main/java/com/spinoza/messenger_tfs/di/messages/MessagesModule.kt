package com.spinoza.messenger_tfs.di.messages

import com.spinoza.messenger_tfs.presentation.feature.messages.MessagesActor
import com.spinoza.messenger_tfs.presentation.feature.messages.MessagesReducer
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenEffect
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenState
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import vivid.money.elmslie.coroutines.ElmStoreCompat

@Module
object MessagesModule {

    @Provides
    fun provideMessagesScreenState(): MessagesScreenState = MessagesScreenState()

    @Provides
    fun profileCoroutineDispatcher(): CoroutineDispatcher = Dispatchers.Default

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