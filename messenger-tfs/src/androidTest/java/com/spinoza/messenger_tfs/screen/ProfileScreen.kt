package com.spinoza.messenger_tfs.screen

import com.kaspersky.kaspresso.screens.KScreen
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.presentation.feature.profile.ProfileFragment
import io.github.kakaocup.kakao.text.KTextView

class ProfileScreen : KScreen<ProfileScreen>() {

    override val layoutId: Int = R.layout.fragment_profile
    override val viewClass: Class<*> = ProfileFragment::class.java

    val username = KTextView { withId(R.id.textViewName) }
}