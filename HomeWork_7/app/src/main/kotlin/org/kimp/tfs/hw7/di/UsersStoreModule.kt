package org.kimp.tfs.hw7.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.kimp.tfs.hw7.domain.ProfilesInteractor
import org.kimp.tfs.hw7.presentation.people.elm.Actor
import org.kimp.tfs.hw7.presentation.people.elm.Effect
import org.kimp.tfs.hw7.presentation.people.elm.Event
import org.kimp.tfs.hw7.presentation.people.elm.Reducer
import org.kimp.tfs.hw7.presentation.people.elm.State
import vivid.money.elmslie.core.store.Store
import vivid.money.elmslie.coroutines.ElmStoreCompat
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UsersStoreModule {

    @Provides
    @Singleton
    fun provideUsersStore(
        actor: Actor
    ): Store<Event, Effect, State> = ElmStoreCompat(
        initialState = State(),
        reducer = Reducer(),
        actor = actor
    )

    @Provides
    fun provideUsersActor(
        profilesInteractor: ProfilesInteractor
    ) = Actor(profilesInteractor)
}
