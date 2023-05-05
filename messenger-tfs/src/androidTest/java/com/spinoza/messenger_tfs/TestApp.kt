package com.spinoza.messenger_tfs

import com.spinoza.messenger_tfs.di.DaggerTestApplicationComponent
import com.spinoza.messenger_tfs.di.app.ApplicationComponent
import com.spinoza.messenger_tfs.presentation.feature.app.App

class TestApp : App() {

    override fun initAppComponent(): ApplicationComponent {
        return DaggerTestApplicationComponent.factory().create(applicationContext)
    }
}