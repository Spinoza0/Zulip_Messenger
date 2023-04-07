package com.spinoza.messenger_tfs.presentation.model.profile

sealed class ProfileEvent {

    sealed class Ui : ProfileEvent() {

        object LoadCurrentUser : Ui()

        object GoBack : Ui()

        class LoadUser(val userId: Long) : Ui()
    }
}