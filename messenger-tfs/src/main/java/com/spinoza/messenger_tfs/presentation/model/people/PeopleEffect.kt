package com.spinoza.messenger_tfs.presentation.model.people

sealed class PeopleEffect {

    sealed class Failure : PeopleEffect() {

        class ErrorNetwork(val value: String) : Failure()

        class ErrorLoadingUsers(val value: String) : Failure()
    }
}