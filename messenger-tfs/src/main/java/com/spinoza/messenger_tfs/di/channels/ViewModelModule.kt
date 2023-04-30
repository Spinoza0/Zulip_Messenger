package com.spinoza.messenger_tfs.di.channels

import androidx.lifecycle.ViewModel
import com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.ChannelsFragmentSharedViewModel
import com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.ChannelsPageFragmentViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
interface ViewModelModule {

    @IntoMap
    @ViewModelKey(ChannelsPageFragmentViewModel::class)
    @Binds
    fun bindChannelsPageFragmentViewModel(impl: ChannelsPageFragmentViewModel): ViewModel

    @IntoMap
    @ViewModelKey(ChannelsFragmentSharedViewModel::class)
    @Binds
    fun bindChannelsFragmentSharedViewModel(impl: ChannelsFragmentSharedViewModel): ViewModel

    companion object {

        @Provides
        fun profileCoroutineDispatcher(): CoroutineDispatcher = Dispatchers.Default
    }
}