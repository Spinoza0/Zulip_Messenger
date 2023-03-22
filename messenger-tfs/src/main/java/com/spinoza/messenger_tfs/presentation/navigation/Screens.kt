package com.spinoza.messenger_tfs.presentation.navigation

import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.presentation.fragment.MainFragment
import com.spinoza.messenger_tfs.presentation.fragment.MessagesFragment
import com.spinoza.messenger_tfs.presentation.fragment.ProfileFragment
import com.spinoza.messenger_tfs.presentation.fragment.menu.ItemChannelsFragment
import com.spinoza.messenger_tfs.presentation.fragment.menu.ItemPeopleFragment
import com.spinoza.messenger_tfs.presentation.fragment.menu.ItemProfileFragment

object Screens {

    fun MainMenu() = FragmentScreen { MainFragment.newInstance() }

    fun Messages(messagesFilter: MessagesFilter) = FragmentScreen {
        MessagesFragment.newInstance(messagesFilter)
    }

    fun UserProfile(userId: Long) = FragmentScreen { ProfileFragment.newInstance(userId) }

    fun ItemChannels() = FragmentScreen { ItemChannelsFragment.newInstance() }

    fun ItemPeople() = FragmentScreen { ItemPeopleFragment.newInstance() }

    fun ItemProfile() = FragmentScreen { ItemProfileFragment.newInstance() }
}