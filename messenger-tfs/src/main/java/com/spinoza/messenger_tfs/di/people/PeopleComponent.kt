package com.spinoza.messenger_tfs.di.people

import androidx.lifecycle.Lifecycle
import com.spinoza.messenger_tfs.di.ApplicationComponent
import com.spinoza.messenger_tfs.di.PeopleScope
import com.spinoza.messenger_tfs.presentation.feature.people.PeopleFragment
import dagger.BindsInstance
import dagger.Component

@PeopleScope
@Component(dependencies = [ApplicationComponent::class], modules = [PeopleModule::class])
interface PeopleComponent {

    fun inject(peopleFragment: PeopleFragment)

    @Component.Factory
    interface Factory {

        fun create(
            applicationComponent: ApplicationComponent,
            @BindsInstance lifecycle: Lifecycle,
        ): PeopleComponent
    }
}