package com.spinoza.messenger_tfs.di.channels

import com.spinoza.messenger_tfs.di.ApplicationComponent
import com.spinoza.messenger_tfs.di.ChannelIsSubscribed
import com.spinoza.messenger_tfs.di.ChannelsScope
import com.spinoza.messenger_tfs.presentation.feature.channels.ChannelsFragment
import com.spinoza.messenger_tfs.presentation.feature.channels.ChannelsPageFragment
import dagger.BindsInstance
import dagger.Component

@ChannelsScope
@Component(dependencies = [ApplicationComponent::class], modules = [ViewModelModule::class])
interface ChannelsComponent {

    fun inject(fragment: ChannelsFragment)

    fun inject(fragment: ChannelsPageFragment)

    @Component.Factory
    interface Factory {

        fun create(
            applicationComponent: ApplicationComponent,
            @ChannelIsSubscribed @BindsInstance isSubscribed: Boolean = true,
        ): ChannelsComponent
    }
}