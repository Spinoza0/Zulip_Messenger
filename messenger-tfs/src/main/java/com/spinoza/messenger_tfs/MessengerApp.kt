package com.spinoza.messenger_tfs

import android.app.Application
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router

class MessengerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val cicerone = Cicerone.create()
        navigatorHolder = cicerone.getNavigatorHolder()
        router = cicerone.router
    }

    companion object {
        lateinit var navigatorHolder: NavigatorHolder
        lateinit var router: Router
    }
}