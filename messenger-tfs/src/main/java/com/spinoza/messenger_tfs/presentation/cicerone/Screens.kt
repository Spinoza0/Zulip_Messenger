package com.spinoza.messenger_tfs.presentation.cicerone

import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.spinoza.messenger_tfs.presentation.fragment.*

object Screens {

    fun Main() = FragmentScreen { MainFragment.newInstance() }

    fun Messages() = FragmentScreen { MessagesFragment.newInstance() }

    fun Channels() = FragmentScreen { ChannelsFragment.newInstance() }

    fun People() = FragmentScreen { PeopleFragment.newInstance() }

    fun Profile() = FragmentScreen { ProfileFragment.newInstance() }
}