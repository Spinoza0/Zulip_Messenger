package com.spinoza.messenger_tfs.presentation.model.people

sealed class PeopleScreenEffect {

    sealed class Failure : PeopleScreenEffect() {

        class ErrorNetwork(val value: String) : Failure()

        class ErrorLoadingUsers(val value: String) : Failure()
    }
}