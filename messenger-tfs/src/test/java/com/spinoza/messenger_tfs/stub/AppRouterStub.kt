package com.spinoza.messenger_tfs.stub

import com.github.terrakok.cicerone.Screen
import com.spinoza.messenger_tfs.presentation.navigation.AppRouter

class AppRouterStub : AppRouter {

    override fun navigateTo(screen: Screen) {}

    override fun replaceScreen(screen: Screen) {}

    override fun exit() {}
}