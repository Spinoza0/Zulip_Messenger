package com.spinoza.messenger_tfs.screen

import com.kaspersky.kaspresso.screens.KScreen
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.presentation.feature.menu.MainMenuFragment
import io.github.kakaocup.kakao.bottomnav.KBottomNavigationView

class MainMenuScreen : KScreen<MainMenuScreen>() {

    override val layoutId: Int = R.layout.fragment_main_menu
    override val viewClass: Class<*> = MainMenuFragment::class.java

    val bottomNavigationView = KBottomNavigationView { withId(R.id.bottomNavigationView) }
}