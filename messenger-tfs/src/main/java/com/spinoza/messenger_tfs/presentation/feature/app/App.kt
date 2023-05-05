package com.spinoza.messenger_tfs.presentation.feature.app

import android.app.Application
import com.spinoza.messenger_tfs.BuildConfig
import com.spinoza.messenger_tfs.di.app.ApplicationComponent
import com.spinoza.messenger_tfs.di.app.DaggerApplicationComponent
import vivid.money.elmslie.android.logger.strategy.AndroidLog
import vivid.money.elmslie.core.config.ElmslieConfig
import vivid.money.elmslie.core.logger.strategy.IgnoreLog

open class App : Application() {

    val appComponent by lazy {
        initAppComponent()
    }

    open fun initAppComponent(): ApplicationComponent =
        DaggerApplicationComponent.factory().create(applicationContext)

    override fun onCreate() {
        super.onCreate()
        ElmslieConfig.apply {
            if (BuildConfig.DEBUG) {
                logger { always(AndroidLog.E) }
            } else {
                logger { always(IgnoreLog) }
            }
        }
    }
}