package com.spinoza.messenger_tfs.di.profile

import androidx.lifecycle.Lifecycle
import com.spinoza.messenger_tfs.di.ApplicationComponent
import com.spinoza.messenger_tfs.presentation.feature.profile.ProfileFragment
import com.spinoza.messenger_tfs.presentation.feature.profile.model.ProfileScreenState
import dagger.BindsInstance
import dagger.Component

@Component(dependencies = [ApplicationComponent::class], modules = [ProfileModule::class])
interface ProfileComponent {

    fun inject(profileFragment: ProfileFragment)

    @Component.Factory
    interface Factory {

        fun create(
            applicationComponent: ApplicationComponent,
            @BindsInstance lifecycle: Lifecycle,
            @BindsInstance initialState: ProfileScreenState,
        ): ProfileComponent
    }
}