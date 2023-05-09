package com.spinoza.messenger_tfs.presentation.navigation

import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router

interface AppNavigatorHolder {

    fun getHolder(): NavigatorHolder

    fun getRouter(): Router
}