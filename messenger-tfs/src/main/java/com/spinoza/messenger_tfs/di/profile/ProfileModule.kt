package com.spinoza.messenger_tfs.di.profile

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.spinoza.messenger_tfs.domain.usecase.event.DeleteEventQueueUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.RegisterEventQueueUseCase
import com.spinoza.messenger_tfs.presentation.feature.profile.ProfileActor
import com.spinoza.messenger_tfs.presentation.feature.profile.ProfileReducer
import com.spinoza.messenger_tfs.presentation.feature.profile.model.ProfileScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.profile.model.ProfileScreenEffect
import com.spinoza.messenger_tfs.presentation.feature.profile.model.ProfileScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.profile.model.ProfileScreenState
import com.spinoza.messenger_tfs.presentation.util.EventsQueueHolder
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import vivid.money.elmslie.coroutines.ElmStoreCompat

@Module
object ProfileModule {

    @Provides
    fun profileCoroutineDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    fun provideEventsQueueHolder(
        lifecycle: Lifecycle,
        registerEventQueueUseCase: RegisterEventQueueUseCase,
        deleteEventQueueUseCase: DeleteEventQueueUseCase,
    ): EventsQueueHolder = EventsQueueHolder(
        lifecycle.coroutineScope,
        registerEventQueueUseCase,
        deleteEventQueueUseCase
    )

    @Provides
    fun provideProfileStore(
        state: ProfileScreenState,
        actor: ProfileActor,
        reducer: ProfileReducer,
    ): ElmStoreCompat<
            ProfileScreenEvent,
            ProfileScreenState,
            ProfileScreenEffect,
            ProfileScreenCommand> =
        ElmStoreCompat(state, reducer, actor)
}