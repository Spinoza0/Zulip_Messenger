package com.spinoza.messenger_tfs.di.menu

import com.spinoza.messenger_tfs.di.ApplicationComponent
import com.spinoza.messenger_tfs.di.MenuScope
import com.spinoza.messenger_tfs.presentation.feature.menu.MainMenuFragment
import dagger.Component

@MenuScope
@Component(dependencies = [ApplicationComponent::class])
interface MenuComponent {

    fun inject(mainMenuFragment: MainMenuFragment)

    @Component.Factory
    interface Factory {

        fun create(
            applicationComponent: ApplicationComponent,
        ): MenuComponent
    }
}