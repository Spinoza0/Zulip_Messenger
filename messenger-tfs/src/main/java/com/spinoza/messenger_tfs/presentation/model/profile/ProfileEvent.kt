package com.spinoza.messenger_tfs.presentation.model.profile

import com.spinoza.messenger_tfs.domain.model.User

sealed class ProfileEvent {

    sealed class Ui : ProfileEvent() {

        object Init : Ui()

        object GoBack : Ui()

        object LoadCurrentUser : Ui()

        class LoadUser(val userId: Long) : Ui()

        class SubscribePresence(val user: User?) : Ui()
    }

    sealed class Internal : ProfileEvent() {

        class UserLoaded(val value: User) : Internal()

        class ErrorNetwork(val value: String) : Internal()

        class ErrorUserLoading(val value: String) : Internal()
    }
}