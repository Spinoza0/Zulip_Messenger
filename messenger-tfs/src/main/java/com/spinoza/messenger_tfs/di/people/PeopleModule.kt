package com.spinoza.messenger_tfs.di.people

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.domain.usecase.DeleteEventQueueUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetPresenceEventsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetUsersByFilterUseCase
import com.spinoza.messenger_tfs.domain.usecase.RegisterEventQueueUseCase
import com.spinoza.messenger_tfs.presentation.feature.app.utils.EventsQueueHolder
import com.spinoza.messenger_tfs.presentation.feature.people.PeopleActor
import com.spinoza.messenger_tfs.presentation.feature.people.PeopleReducer
import com.spinoza.messenger_tfs.presentation.feature.people.model.PeopleScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.people.model.PeopleScreenEffect
import com.spinoza.messenger_tfs.presentation.feature.people.model.PeopleScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.people.model.PeopleScreenState
import dagger.Module
import dagger.Provides
import vivid.money.elmslie.coroutines.ElmStoreCompat

@Module
class PeopleModule {

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
    fun providePeopleActor(
        lifecycle: Lifecycle, getUsersByFilterUseCase: GetUsersByFilterUseCase,
        getPresenceEventsUseCase: GetPresenceEventsUseCase,
        eventsQueue: EventsQueueHolder,
    ): PeopleActor =
        PeopleActor(lifecycle, getUsersByFilterUseCase, getPresenceEventsUseCase, eventsQueue)

    @Provides
    fun providePeopleReducer(router: Router): PeopleReducer = PeopleReducer(router)

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