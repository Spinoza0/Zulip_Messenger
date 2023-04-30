package com.spinoza.messenger_tfs.di.channels

import androidx.lifecycle.ViewModel
import com.spinoza.messenger_tfs.di.ChannelIsSubscribed
import com.spinoza.messenger_tfs.domain.usecase.channels.GetChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.channels.GetStoredChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.channels.GetStoredTopicsUseCase
import com.spinoza.messenger_tfs.domain.usecase.channels.GetTopicUseCase
import com.spinoza.messenger_tfs.domain.usecase.channels.GetTopicsUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.DeleteEventQueueUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.GetChannelEventsUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.RegisterEventQueueUseCase
import com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.ChannelsFragmentSharedViewModel
import com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.ChannelsPageFragmentViewModel
import com.spinoza.messenger_tfs.presentation.navigation.AppRouter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
interface ViewModelModule {

    @IntoMap
    @ViewModelKey(ChannelsFragmentSharedViewModel::class)
    @Binds
    fun bindChannelsFragmentSharedViewModel(impl: ChannelsFragmentSharedViewModel): ViewModel

    companion object {

        @Provides
        fun profileCoroutineDispatcher(): CoroutineDispatcher = Dispatchers.Default

        @IntoMap
        @ViewModelKey(ChannelsPageFragmentViewModel::class)
        @Provides
        fun provideChannelsPageFragmentViewModel(
            @ChannelIsSubscribed isSubscribed: Boolean,
            router: AppRouter,
            getStoredTopicsUseCase: GetStoredTopicsUseCase,
            getTopicsUseCase: GetTopicsUseCase,
            getStoredChannelsUseCase: GetStoredChannelsUseCase,
            getChannelsUseCase: GetChannelsUseCase,
            getTopicUseCase: GetTopicUseCase,
            getChannelEventsUseCase: GetChannelEventsUseCase,
            registerEventQueueUseCase: RegisterEventQueueUseCase,
            deleteEventQueueUseCase: DeleteEventQueueUseCase,
            defaultDispatcher: CoroutineDispatcher,
        ): ViewModel {
            return ChannelsPageFragmentViewModel(
                isSubscribed,
                router,
                getStoredTopicsUseCase,
                getTopicsUseCase,
                getStoredChannelsUseCase,
                getChannelsUseCase,
                getTopicUseCase,
                getChannelEventsUseCase,
                registerEventQueueUseCase,
                deleteEventQueueUseCase,
                defaultDispatcher,
                null
            )
        }
    }
}