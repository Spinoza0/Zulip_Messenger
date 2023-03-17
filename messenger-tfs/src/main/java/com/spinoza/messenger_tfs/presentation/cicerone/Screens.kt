package com.spinoza.messenger_tfs.presentation.cicerone

import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.spinoza.messenger_tfs.presentation.fragment.menu.MainChannelsFragment
import com.spinoza.messenger_tfs.presentation.fragment.menu.MainPeopleFragment
import com.spinoza.messenger_tfs.presentation.fragment.menu.MainProfileFragment
import com.spinoza.messenger_tfs.presentation.fragment.MessagesFragment

object Screens {

    fun Messages(channelId: Long, topicName: String) = FragmentScreen {
        MessagesFragment.newInstance(channelId, topicName)
    }

    fun Channels() = FragmentScreen { MainChannelsFragment.newInstance() }

    fun People() = FragmentScreen { MainPeopleFragment.newInstance() }

    fun Profile() = FragmentScreen { MainProfileFragment.newInstance() }
}