package com.spinoza.messenger_tfs.presentation.feature.people.model

import com.spinoza.messenger_tfs.domain.model.User

sealed class PeopleEvent {

    sealed class Ui : PeopleEvent() {

        object Init : Ui()

        object Load : Ui()

        class Filter(val value: String) : Ui()

        class ShowUserInfo(val userId: Long) : Ui()

        object OpenMainMenu : Ui()
    }

    sealed class Internal : PeopleEvent() {

        object Idle : Internal()

        object EmptyQueueEvent : Internal()

        object FilterChanged : Internal()

        class UsersLoaded(val value: List<User>) : Internal()

        class EventFromQueue(val value: List<User>) : Internal()

        class ErrorNetwork(val value: String) : Internal()

        class ErrorUserLoading(val value: String) : Internal()
    }
}