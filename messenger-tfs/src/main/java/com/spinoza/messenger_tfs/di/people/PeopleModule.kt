package com.spinoza.messenger_tfs.di.people

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.spinoza.messenger_tfs.domain.usecase.event.DeleteEventQueueUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.RegisterEventQueueUseCase
import com.spinoza.messenger_tfs.presentation.feature.people.PeopleActor
import com.spinoza.messenger_tfs.presentation.feature.people.PeopleReducer
import com.spinoza.messenger_tfs.presentation.feature.people.model.PeopleScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.people.model.PeopleScreenEffect
import com.spinoza.messenger_tfs.presentation.feature.people.model.PeopleScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.people.model.PeopleScreenState
import com.spinoza.messenger_tfs.presentation.util.EventsQueueHolder
import dagger.Module
import dagger.Provides
import vivid.money.elmslie.coroutines.ElmStoreCompat

@Module
object PeopleModule {

    @Provides
    fun providePeopleScreenState(): PeopleScreenState = PeopleScreenState()

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
    fun providePeopleStore(
        state: PeopleScreenState,
        actor: PeopleActor,
        reducer: PeopleReducer,
    ): ElmStoreCompat<
            PeopleScreenEvent,
            PeopleScreenState,
            PeopleScreenEffect,
            PeopleScreenCommand> =
        ElmStoreCompat(state, reducer, actor)
}