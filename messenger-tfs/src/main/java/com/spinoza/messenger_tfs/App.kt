package com.spinoza.messenger_tfs

import android.app.Application
import com.spinoza.messenger_tfs.di.GlobalDI
import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import vivid.money.elmslie.android.logger.strategy.AndroidLog
import vivid.money.elmslie.core.config.ElmslieConfig
import vivid.money.elmslie.core.logger.strategy.IgnoreLog

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val cicerone = Cicerone.create()
        navigatorHolder = cicerone.getNavigatorHolder()
        router = cicerone.router

        GlobalDI.init()

        ElmslieConfig.apply {
            if (BuildConfig.DEBUG) {
                logger { always(AndroidLog.E) }
            } else {
                logger { always(IgnoreLog) }
            }
        }
    }

    companion object {
        lateinit var navigatorHolder: NavigatorHolder
        lateinit var router: Router
    }
}