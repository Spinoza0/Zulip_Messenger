package com.spinoza.messenger_tfs.di.login

import androidx.lifecycle.LifecycleCoroutineScope
import com.spinoza.messenger_tfs.di.ApplicationComponent
import com.spinoza.messenger_tfs.di.LoginScope
import com.spinoza.messenger_tfs.presentation.feature.login.LoginFragment
import dagger.BindsInstance
import dagger.Component

@LoginScope
@Component(dependencies = [ApplicationComponent::class], modules = [LoginModule::class])
interface LoginComponent {

    fun inject(loginFragment: LoginFragment)

    @Component.Factory
    interface Factory {

        fun create(
            applicationComponent: ApplicationComponent,
            @BindsInstance lifecycleCoroutineScope: LifecycleCoroutineScope,
        ): LoginComponent
    }
}