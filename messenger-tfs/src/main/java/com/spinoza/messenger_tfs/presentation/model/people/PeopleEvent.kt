package com.spinoza.messenger_tfs.presentation.model.people

sealed class PeopleEvent {

    sealed class Ui : PeopleEvent() {

        object Load : Ui()

        class Filter(val value: String) : Ui()

        class ShowUserInfo(val userId: Long) : Ui()

        object OpenMainMenu : Ui()
    }
}