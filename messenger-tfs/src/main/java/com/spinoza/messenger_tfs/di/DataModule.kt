package com.spinoza.messenger_tfs.di

import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import dagger.Module
import dagger.Provides

@Module
class DataModule {

    @Provides
    fun provideMessagesRepository(): MessagesRepository = MessagesRepositoryImpl.getInstance()
}