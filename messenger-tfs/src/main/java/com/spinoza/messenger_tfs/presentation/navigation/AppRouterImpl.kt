package com.spinoza.messenger_tfs.presentation.navigation

import com.github.terrakok.cicerone.Screen
import javax.inject.Inject

class AppRouterImpl @Inject constructor(appNavigatorHolder: AppNavigatorHolder) : AppRouter {

    private val router = appNavigatorHolder.getRouter()

    override fun navigateTo(screen: Screen) {
        router.navigateTo(screen)
    }

    override fun replaceScreen(screen: Screen) {
        router.replaceScreen(screen)
    }

    override fun exit() {
        router.exit()
    }
}