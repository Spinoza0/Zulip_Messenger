package com.spinoza.messenger_tfs

import android.app.Application
import com.cyberfox21.tinkofffintechseminar.di.GlobalDI
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val cicerone = Cicerone.create()
        navigatorHolder = cicerone.getNavigatorHolder()
        router = cicerone.router

        GlobalDI.init()
    }

    companion object {
        lateinit var navigatorHolder: NavigatorHolder
        lateinit var router: Router
    }
}