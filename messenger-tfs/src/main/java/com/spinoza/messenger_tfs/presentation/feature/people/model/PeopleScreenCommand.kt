package com.spinoza.messenger_tfs.presentation.feature.people.model

sealed class PeopleScreenCommand {

    object GetFilteredList : PeopleScreenCommand()

    object GetEvent : PeopleScreenCommand()

    object Load : PeopleScreenCommand()

    class SetNewFilter(val filter: String) : PeopleScreenCommand()
}