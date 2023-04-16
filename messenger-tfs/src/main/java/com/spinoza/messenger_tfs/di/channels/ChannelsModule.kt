package com.spinoza.messenger_tfs.di.channels

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.di.ChannelIsSubscribed
import com.spinoza.messenger_tfs.di.GlobalRouter
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.presentation.feature.app.App
import com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.ChannelsFragmentSharedViewModel
import com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.ChannelsPageFragmentViewModel
import com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.factory.ChannelsPageFragmentViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class ChannelsModule {

    @GlobalRouter
    @Provides
    fun provideGlobalRouter(): Router = App.router

    @ChannelIsSubscribed
    @Provides
    fun provideChannelIsSubscribed(isSubscribed: Boolean): Boolean = isSubscribed

    @Provides
    fun provideMessagesRepository(): MessagesRepository = MessagesRepositoryImpl.getInstance()

    @Provides
    fun provideChannelsPageFragmentViewModel(
        fragment: Fragment,
        channelsPageFragmentViewModelFactory: ChannelsPageFragmentViewModelFactory,
    ): ChannelsPageFragmentViewModel =
        ViewModelProvider(
            fragment,
            channelsPageFragmentViewModelFactory
        )[ChannelsPageFragmentViewModel::class.java]

    @Provides
    fun provideChannelsFragmentSharedViewModel(
        activity: Activity,
    ): ChannelsFragmentSharedViewModel =
        ViewModelProvider(activity as AppCompatActivity)[ChannelsFragmentSharedViewModel::class.java]
}