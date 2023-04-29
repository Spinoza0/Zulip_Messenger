package com.spinoza.messenger_tfs.presentation.navigation

import com.github.terrakok.cicerone.Screen

interface AppRouter {

    fun navigateTo(screen: Screen)

    fun replaceScreen(screen: Screen)

    fun exit()
}