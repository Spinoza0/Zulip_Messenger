package com.spinoza.messenger_tfs.presentation.cicerone

import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.presentation.fragment.MessagesFragment
import com.spinoza.messenger_tfs.presentation.fragment.menu.MainChannelsFragment
import com.spinoza.messenger_tfs.presentation.fragment.menu.MainPeopleFragment
import com.spinoza.messenger_tfs.presentation.fragment.menu.MainProfileFragment

object Screens {

    fun Messages(channel: Channel, topicName: String) = FragmentScreen {
        MessagesFragment.newInstance(channel, topicName)
    }

    fun Channels() = FragmentScreen { MainChannelsFragment.newInstance() }

    fun People() = FragmentScreen { MainPeopleFragment.newInstance() }

    fun Profile() = FragmentScreen { MainProfileFragment.newInstance() }
}