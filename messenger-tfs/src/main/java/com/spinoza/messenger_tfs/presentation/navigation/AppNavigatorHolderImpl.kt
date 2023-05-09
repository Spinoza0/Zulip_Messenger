package com.spinoza.messenger_tfs.presentation.navigation

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import javax.inject.Inject

class AppNavigatorHolderImpl @Inject constructor(): AppNavigatorHolder {

    private val cicerone = Cicerone.create()
    private val navigatorHolder = cicerone.getNavigatorHolder()

    override fun getHolder(): NavigatorHolder = navigatorHolder

    override fun getRouter(): Router = cicerone.router
}