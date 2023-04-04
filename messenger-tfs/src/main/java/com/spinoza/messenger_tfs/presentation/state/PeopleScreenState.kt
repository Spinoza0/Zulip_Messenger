package com.spinoza.messenger_tfs.presentation.state

import com.spinoza.messenger_tfs.domain.model.User

sealed class PeopleScreenState {

    object Start: PeopleScreenState()

    object Loading : PeopleScreenState()

    class Users(val value: List<User>) : PeopleScreenState()

    sealed class Failure : PeopleScreenState() {

        class LoadingUsers(val value: String) : Failure()

        class Network(val value: String) : Failure()
    }
}