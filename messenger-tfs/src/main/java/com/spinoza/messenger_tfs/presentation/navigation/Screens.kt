package com.spinoza.messenger_tfs.presentation.navigation

import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.presentation.fragment.MainFragment
import com.spinoza.messenger_tfs.presentation.fragment.MessagesFragment
import com.spinoza.messenger_tfs.presentation.fragment.UserProfileFragment
import com.spinoza.messenger_tfs.presentation.fragment.menu.ChannelsFragment
import com.spinoza.messenger_tfs.presentation.fragment.menu.PeopleFragment
import com.spinoza.messenger_tfs.presentation.fragment.menu.OwnUserProfileFragment

object Screens {

    fun MainMenu() = FragmentScreen { MainFragment.newInstance() }

    fun Messages(messagesFilter: MessagesFilter) = FragmentScreen {
        MessagesFragment.newInstance(messagesFilter)
    }

    fun UserProfile(userId: Long) = FragmentScreen { UserProfileFragment.newInstance(userId) }

    fun ItemChannels() = FragmentScreen { ChannelsFragment.newInstance() }

    fun ItemPeople() = FragmentScreen { PeopleFragment.newInstance() }

    fun ItemProfile() = FragmentScreen { OwnUserProfileFragment.newInstance() }
}