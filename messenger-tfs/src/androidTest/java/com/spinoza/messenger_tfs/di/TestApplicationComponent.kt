package com.spinoza.messenger_tfs.di

import android.content.Context
import com.spinoza.messenger_tfs.di.app.ApplicationComponent
import com.spinoza.messenger_tfs.di.app.ApplicationModule
import com.spinoza.messenger_tfs.di.app.DataModule
import dagger.BindsInstance
import dagger.Component

@ApplicationScope
@Component(modules = [ApplicationModule::class, DataModule::class, TestStorageModule::class])
interface TestApplicationComponent : ApplicationComponent {

    @Component.Factory
    interface Factory {

        fun create(@BindsInstance context: Context): ApplicationComponent
    }
}