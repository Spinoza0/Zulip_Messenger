package com.spinoza.messenger_tfs.di.messages

import androidx.lifecycle.Lifecycle
import com.spinoza.messenger_tfs.di.ApplicationComponent
import com.spinoza.messenger_tfs.presentation.feature.messages.MessagesFragment
import dagger.BindsInstance
import dagger.Component

@Component(dependencies = [ApplicationComponent::class], modules = [MessagesModule::class])
interface MessagesComponent {

    fun inject(messagesFragment: MessagesFragment)

    @Component.Factory
    interface Factory {

        fun create(
            applicationComponent: ApplicationComponent,
            @BindsInstance lifecycle: Lifecycle,
        ): MessagesComponent
    }
}