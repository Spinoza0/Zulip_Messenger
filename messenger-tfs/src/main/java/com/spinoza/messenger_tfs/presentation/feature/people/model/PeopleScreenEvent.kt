package com.spinoza.messenger_tfs.presentation.feature.people.model

import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.domain.model.User

sealed class PeopleScreenEvent {

    sealed class Ui : PeopleScreenEvent() {

        object Init : Ui()

        object Load : Ui()

        class Filter(val value: String) : Ui()

        class ShowUserInfo(val userId: Long) : Ui()

        class OnScrolled(val recyclerView: RecyclerView, val dy: Int) : Ui()

        object OpenMainMenu : Ui()
    }

    sealed class Internal : PeopleScreenEvent() {

        object Idle : Internal()

        object EmptyQueueEvent : Internal()

        object FilterChanged : Internal()

        class UsersLoaded(val value: List<User>) : Internal()

        class EventFromQueue(val value: List<User>) : Internal()

        class ErrorNetwork(val value: String) : Internal()

        class ErrorUserLoading(val value: String) : Internal()
    }
}