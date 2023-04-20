package org.kimp.tfs.hw7.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.kimp.tfs.hw7.data.ChannelsRepository
import org.kimp.tfs.hw7.data.ProfilesRepository
import org.kimp.tfs.hw7.data.ZulipService

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {
    @Provides
    fun provideChannelsRepository(
        zulipService: ZulipService
    ) = ChannelsRepository(zulipService)

    @Provides
    fun provideProfilesRepository(
        zulipService: ZulipService
    ) = ProfilesRepository(zulipService)
}
