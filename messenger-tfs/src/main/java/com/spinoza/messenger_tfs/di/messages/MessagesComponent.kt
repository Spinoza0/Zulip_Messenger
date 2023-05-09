package com.spinoza.messenger_tfs.di.messages

import com.spinoza.messenger_tfs.di.MessagesScope
import com.spinoza.messenger_tfs.di.app.ApplicationComponent
import com.spinoza.messenger_tfs.presentation.feature.messages.MessagesFragment
import dagger.BindsInstance
import dagger.Component

@MessagesScope
@Component(dependencies = [ApplicationComponent::class], modules = [MessagesModule::class])
interface MessagesComponent {

    fun inject(messagesFragment: MessagesFragment)

    @Component.Factory
    interface Factory {

        fun create(
            applicationComponent: ApplicationComponent,
            @BindsInstance fragment: MessagesFragment,
        ): MessagesComponent
    }
}