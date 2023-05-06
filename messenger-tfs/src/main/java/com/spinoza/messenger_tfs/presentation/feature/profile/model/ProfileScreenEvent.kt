package com.spinoza.messenger_tfs.presentation.feature.profile.model

import com.spinoza.messenger_tfs.domain.model.User

sealed class ProfileScreenEvent {

    sealed class Ui : ProfileScreenEvent() {

        object Idle : Ui()

        object CheckLoginStatus : Ui()

        object GoBack : Ui()

        object Logout : Ui()

        object LoadCurrentUser : Ui()

        class LoadUser(val userId: Long) : Ui()

        class SubscribePresence(val user: User?) : Ui()
    }

    sealed class Internal : ProfileScreenEvent() {

        object Idle : Internal()

        object EmptyQueueEvent : Internal()

        object LoginSuccess : Internal()

        object LogOut : Internal()

        class UserLoaded(val value: User) : Internal()

        class ErrorNetwork(val value: String) : Internal()

        class ErrorUserLoading(val value: String) : Internal()
    }
}