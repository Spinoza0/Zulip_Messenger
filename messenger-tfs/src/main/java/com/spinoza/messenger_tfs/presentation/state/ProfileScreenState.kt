package com.spinoza.messenger_tfs.presentation.state

import com.spinoza.messenger_tfs.domain.model.User

sealed class ProfileScreenState {

    object Idle : ProfileScreenState()

    object Loading : ProfileScreenState()

    class UserData(val value: User) : ProfileScreenState()

    class Presence(val value: User.Presence) : ProfileScreenState()

    sealed class Failure : ProfileScreenState() {

        class Network(val value: String) : Failure()

        class UserNotFound(val userId: Long, val value: String) : Failure()
    }
}