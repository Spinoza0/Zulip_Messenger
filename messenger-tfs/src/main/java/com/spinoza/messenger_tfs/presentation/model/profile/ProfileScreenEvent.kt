package com.spinoza.messenger_tfs.presentation.model.profile

import com.spinoza.messenger_tfs.domain.model.User

sealed class ProfileScreenEvent {

    sealed class Ui : ProfileScreenEvent() {

        object Init : Ui()

        object GoBack : Ui()

        object LoadCurrentUser : Ui()

        class LoadUser(val userId: Long) : Ui()

        class SubscribePresence(val user: User?) : Ui()
    }

    sealed class Internal : ProfileScreenEvent() {

        object Idle : Internal()

        object EmptyQueueEvent : Internal()

        class UserLoaded(val value: User) : Internal()

        class ErrorNetwork(val value: String) : Internal()

        class ErrorUserLoading(val value: String) : Internal()
    }
}