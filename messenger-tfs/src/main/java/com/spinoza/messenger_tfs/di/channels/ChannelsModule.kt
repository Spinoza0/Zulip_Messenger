package com.spinoza.messenger_tfs.di.channels

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.spinoza.messenger_tfs.di.ChannelIsSubscribed
import com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.ChannelsFragmentSharedViewModel
import com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.ChannelsPageFragmentViewModel
import com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.factory.ChannelsPageFragmentViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class ChannelsModule {

    @ChannelIsSubscribed
    @Provides
    fun provideChannelIsSubscribed(isSubscribed: Boolean): Boolean = isSubscribed

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