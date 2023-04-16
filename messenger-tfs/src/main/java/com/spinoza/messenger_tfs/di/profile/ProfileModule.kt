package com.spinoza.messenger_tfs.di.profile

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.feature.app.utils.EventsQueueHolder
import com.spinoza.messenger_tfs.presentation.feature.profile.ProfileActor
import com.spinoza.messenger_tfs.presentation.feature.profile.ProfileReducer
import com.spinoza.messenger_tfs.presentation.feature.profile.model.ProfileScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.profile.model.ProfileScreenEffect
import com.spinoza.messenger_tfs.presentation.feature.profile.model.ProfileScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.profile.model.ProfileScreenState
import dagger.Module
import dagger.Provides
import vivid.money.elmslie.coroutines.ElmStoreCompat

@Module
class ProfileModule {

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
    fun provideProfileActor(
        lifecycle: Lifecycle,
        getOwnUserUseCase: GetOwnUserUseCase,
        getUserUseCase: GetUserUseCase,
        getPresenceEventsUseCase: GetPresenceEventsUseCase,
        eventsQueueHolder: EventsQueueHolder,
    ): ProfileActor =
        ProfileActor(
            lifecycle,
            getOwnUserUseCase,
            getUserUseCase,
            getPresenceEventsUseCase,
            eventsQueueHolder
        )

    @Provides
    fun provideProfileReducer(router: Router): ProfileReducer = ProfileReducer(router)

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