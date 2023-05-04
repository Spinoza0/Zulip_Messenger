package com.spinoza.messenger_tfs.di.messages

import androidx.lifecycle.Lifecycle
import com.spinoza.messenger_tfs.presentation.feature.messages.MessagesActor
import com.spinoza.messenger_tfs.presentation.feature.messages.MessagesFragment
import com.spinoza.messenger_tfs.presentation.feature.messages.MessagesReducer
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenEffect
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenState
import com.spinoza.messenger_tfs.presentation.notification.Notificator
import com.spinoza.messenger_tfs.presentation.notification.NotificatorImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import vivid.money.elmslie.coroutines.ElmStoreCompat

@Module
interface MessagesModule {

    @Binds
    fun bindNotificator(impl: NotificatorImpl): Notificator

    companion object {

        @Provides
        fun provideLifecycle(fragment: MessagesFragment): Lifecycle = fragment.lifecycle

        @Provides
        fun provideMessagesScreenState(): MessagesScreenState = MessagesScreenState()

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
}