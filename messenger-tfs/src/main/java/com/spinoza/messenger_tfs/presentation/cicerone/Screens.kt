package com.spinoza.messenger_tfs.presentation.cicerone

import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.presentation.fragment.MainFragment
import com.spinoza.messenger_tfs.presentation.fragment.MessagesFragment
import com.spinoza.messenger_tfs.presentation.fragment.ProfileFragment
import com.spinoza.messenger_tfs.presentation.fragment.menu.ItemChannelsFragment
import com.spinoza.messenger_tfs.presentation.fragment.menu.ItemPeopleFragment
import com.spinoza.messenger_tfs.presentation.fragment.menu.ItemProfileFragment

object Screens {

    fun MainMenu() = FragmentScreen { MainFragment.newInstance() }

    fun Messages(channel: Channel, topicName: String) = FragmentScreen {
        MessagesFragment.newInstance(channel, topicName)
    }

    fun UserProfile(userId: Long) = FragmentScreen { ProfileFragment.newInstance(userId) }

    fun ItemChannels() = FragmentScreen { ItemChannelsFragment.newInstance() }

    fun ItemPeople() = FragmentScreen { ItemPeopleFragment.newInstance() }

    fun ItemProfile() = FragmentScreen { ItemProfileFragment.newInstance() }
}