package com.spinoza.messenger_tfs.di.menu

import com.spinoza.messenger_tfs.di.ApplicationComponent
import com.spinoza.messenger_tfs.presentation.feature.menu.MainMenuFragment
import dagger.BindsInstance
import dagger.Component

@Component(dependencies = [ApplicationComponent::class], modules = [MenuModule::class])
interface MenuComponent {

    fun inject(mainMenuFragment: MainMenuFragment)

    @Component.Factory
    interface Factory {

        fun create(
            applicationComponent: ApplicationComponent,
            @BindsInstance fragment: MainMenuFragment,
        ): MenuComponent
    }
}