package com.spinoza.messenger_tfs.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component

@Component(modules = [ApplicationModule::class, DataModule::class])
interface ApplicationComponent {

    fun context(): Context

    @Component.Factory
    interface Factory {

        fun create(@BindsInstance context: Context): ApplicationComponent
    }
}