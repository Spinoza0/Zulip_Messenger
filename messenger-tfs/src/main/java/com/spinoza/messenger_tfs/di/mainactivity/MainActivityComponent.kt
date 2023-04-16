package com.spinoza.messenger_tfs.di.mainactivity

import com.spinoza.messenger_tfs.di.ApplicationComponent
import com.spinoza.messenger_tfs.presentation.feature.app.MainActivity
import dagger.BindsInstance
import dagger.Component

@Component(dependencies = [ApplicationComponent::class], modules = [MainActivityModule::class])
interface MainActivityComponent {

    fun inject(mainActivity: MainActivity)

    @Component.Factory
    interface Factory {

        fun create(
            applicationComponent: ApplicationComponent,
            @BindsInstance mainActivity: MainActivity,
        ): MainActivityComponent
    }
}