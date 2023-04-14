package com.spinoza.messenger_tfs.presentation.model.people

sealed class PeopleScreenCommand {

    object GetFilteredList : PeopleScreenCommand()

    object GetEvent : PeopleScreenCommand()

    object Load : PeopleScreenCommand()

    class SetNewFilter(val filter: String) : PeopleScreenCommand()
}