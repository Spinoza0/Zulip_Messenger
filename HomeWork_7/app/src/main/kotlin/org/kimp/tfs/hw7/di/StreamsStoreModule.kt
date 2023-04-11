package org.kimp.tfs.hw7.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.kimp.tfs.hw7.data.ChannelsRepository
import org.kimp.tfs.hw7.domain.ChannelsInteractor
import org.kimp.tfs.hw7.domain.ChannelsInteractorImpl
import org.kimp.tfs.hw7.presentation.streams.elm.Actor
import org.kimp.tfs.hw7.presentation.streams.elm.Effect
import org.kimp.tfs.hw7.presentation.streams.elm.Event
import org.kimp.tfs.hw7.presentation.streams.elm.Reducer
import org.kimp.tfs.hw7.presentation.streams.elm.State
import vivid.money.elmslie.core.store.Store
import vivid.money.elmslie.coroutines.ElmStoreCompat

@Module
@InstallIn(SingletonComponent::class)
object StreamsStoreModule {
    @Provides
    fun provideChannelsStore(
        actor: Actor
    ): Store<Event, Effect, State> = ElmStoreCompat(
        initialState = State(),
        reducer = Reducer(),
        actor = actor
    )

    @Provides
    fun provideChannelsActor(
        channelsInteractor: ChannelsInteractor
    ) = Actor(channelsInteractor)

    @Provides
    fun provideChannelsInteractor(
        channelsRepository: ChannelsRepository
    ): ChannelsInteractor = ChannelsInteractorImpl(channelsRepository)
}