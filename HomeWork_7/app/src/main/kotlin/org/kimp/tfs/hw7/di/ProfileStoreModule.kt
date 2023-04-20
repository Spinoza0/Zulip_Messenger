package org.kimp.tfs.hw7.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.kimp.tfs.hw7.data.ProfilesRepository
import org.kimp.tfs.hw7.domain.ProfilesInteractor
import org.kimp.tfs.hw7.domain.ProfilesInteractorImpl
import org.kimp.tfs.hw7.presentation.profile.elm.Actor
import org.kimp.tfs.hw7.presentation.profile.elm.Effect
import org.kimp.tfs.hw7.presentation.profile.elm.Event
import org.kimp.tfs.hw7.presentation.profile.elm.Reducer
import org.kimp.tfs.hw7.presentation.profile.elm.State
import vivid.money.elmslie.core.store.Store
import vivid.money.elmslie.coroutines.ElmStoreCompat
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProfileStoreModule {

    @Provides
    @Singleton
    fun provideProfileStore(
        actor: Actor
    ): Store<Event, Effect, State> = ElmStoreCompat(
        initialState = State(),
        reducer = Reducer(),
        actor = actor
    )

    @Provides
    fun provideProfileActor(
        profilesInteractor: ProfilesInteractor
    ) = Actor(profilesInteractor)


    @Provides
    fun provideProfileInteractor(
        profilesRepository: ProfilesRepository
    ): ProfilesInteractor = ProfilesInteractorImpl(profilesRepository)
}
