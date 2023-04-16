package com.spinoza.messenger_tfs.di.channels

import android.app.Activity
import androidx.fragment.app.Fragment
import com.spinoza.messenger_tfs.di.ApplicationComponent
import com.spinoza.messenger_tfs.presentation.feature.channels.ChannelsFragment
import com.spinoza.messenger_tfs.presentation.feature.channels.ChannelsPageFragment
import dagger.BindsInstance
import dagger.Component

@Component(dependencies = [ApplicationComponent::class], modules = [ChannelsModule::class])
interface ChannelsComponent {

    fun inject(fragment: ChannelsFragment)

    fun inject(fragment: ChannelsPageFragment)

    @Component.Factory
    interface Factory {

        fun create(
            applicationComponent: ApplicationComponent,
            @BindsInstance activity: Activity,
            @BindsInstance fragment: Fragment,
            @BindsInstance isSubscribed: Boolean = true
        ): ChannelsComponent
    }
}