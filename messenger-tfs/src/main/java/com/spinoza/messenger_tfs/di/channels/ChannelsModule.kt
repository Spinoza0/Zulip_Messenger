package com.spinoza.messenger_tfs.di.channels

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.spinoza.messenger_tfs.presentation.feature.channels.adapter.ChannelsPagerAdapter
import com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.ChannelsFragmentSharedViewModel
import com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.ChannelsPageFragmentViewModel
import dagger.Module
import dagger.Provides

@Module
class ChannelsModule {

    @Provides
    fun provideChannelsPagerAdapter(fragment: Fragment) = ChannelsPagerAdapter(fragment)

    @Provides
    fun provideChannelsPageFragmentViewModel(fragment: Fragment): ChannelsPageFragmentViewModel =
        ViewModelProvider(fragment)[ChannelsPageFragmentViewModel::class.java]

    @Provides
    fun provideChannelsFragmentSharedViewModel(
        activity: Activity,
    ): ChannelsFragmentSharedViewModel =
        ViewModelProvider(activity as AppCompatActivity)[ChannelsFragmentSharedViewModel::class.java]
}