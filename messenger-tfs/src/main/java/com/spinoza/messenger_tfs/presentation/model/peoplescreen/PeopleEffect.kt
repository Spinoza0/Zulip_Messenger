package com.spinoza.messenger_tfs.presentation.model.peoplescreen

sealed class PeopleEffect {

    sealed class Failure : PeopleEffect() {

        class Network(val value: String) : Failure()

        class LoadingUsers(val value: String) : Failure()
    }
}