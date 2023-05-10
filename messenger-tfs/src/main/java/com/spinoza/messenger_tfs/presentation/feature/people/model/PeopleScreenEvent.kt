package com.spinoza.messenger_tfs.presentation.feature.people.model

import com.spinoza.messenger_tfs.domain.model.User

sealed class PeopleScreenEvent {

    sealed class Ui : PeopleScreenEvent() {

        object Init : Ui()

        object CheckLoginStatus : Ui()

        object Load : Ui()

        class Filter(val value: String) : Ui()

        class ShowUserInfo(val userId: Long) : Ui()

        object OnScrolled : Ui()

        object ScrollStateDragging : Ui()

        class ScrollStateIdle(val canScrollUp: Boolean, val canScrollDown: Boolean) : Ui()

        object OpenMainMenu : Ui()
    }

    sealed class Internal : PeopleScreenEvent() {

        object Idle : Internal()

        object LoginSuccess : Internal()

        object LogOut : Internal()

        object EmptyQueueEvent : Internal()

        object FilterChanged : Internal()

        class UsersLoaded(val value: List<User>) : Internal()

        class EventFromQueue(val value: List<User>) : Internal()

        class ErrorNetwork(val value: String) : Internal()

        class ErrorUserLoading(val value: String) : Internal()
    }
}