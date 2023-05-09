package com.spinoza.messenger_tfs.presentation.navigation

import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.presentation.feature.login.LoginFragment
import com.spinoza.messenger_tfs.presentation.feature.menu.MainMenuFragment
import com.spinoza.messenger_tfs.presentation.feature.messages.MessagesFragment
import com.spinoza.messenger_tfs.presentation.feature.profile.UserProfileFragment
import com.spinoza.messenger_tfs.presentation.feature.channels.ChannelsFragment
import com.spinoza.messenger_tfs.presentation.feature.profile.OwnUserProfileFragment
import com.spinoza.messenger_tfs.presentation.feature.people.PeopleFragment

object Screens {

    fun Login(logout: Boolean = false) = FragmentScreen { LoginFragment.newInstance(logout) }

    fun MainMenu() = FragmentScreen { MainMenuFragment.newInstance() }

    fun Messages(messagesFilter: MessagesFilter) = FragmentScreen {
        MessagesFragment.newInstance(messagesFilter)
    }

    fun UserProfile(userId: Long) = FragmentScreen { UserProfileFragment.newInstance(userId) }

    fun Channels() = FragmentScreen { ChannelsFragment.newInstance() }

    fun People() = FragmentScreen { PeopleFragment.newInstance() }

    fun OwnUserProfile() = FragmentScreen { OwnUserProfileFragment.newInstance() }
}