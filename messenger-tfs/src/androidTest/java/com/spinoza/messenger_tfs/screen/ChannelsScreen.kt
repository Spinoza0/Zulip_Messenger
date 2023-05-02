package com.spinoza.messenger_tfs.screen

import com.kaspersky.kaspresso.screens.KScreen
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.presentation.feature.channels.ChannelsFragment
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.tabs.KTabLayout

class ChannelsScreen : KScreen<ChannelsScreen>() {

    override val layoutId: Int = R.layout.fragment_channels
    override val viewClass: Class<*> = ChannelsFragment::class.java

    val searchField = KEditText { withId(R.id.editTextSearch) }
    val tabLayout = KTabLayout { withId(R.id.tabLayout) }
}