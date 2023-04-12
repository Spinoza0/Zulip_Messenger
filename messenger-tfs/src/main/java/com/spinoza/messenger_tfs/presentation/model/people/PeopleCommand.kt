package com.spinoza.messenger_tfs.presentation.model.people

sealed class PeopleCommand {

    object GetFilteredList : PeopleCommand()

    object GetEvent : PeopleCommand()

    object Load : PeopleCommand()

    class SetNewFilter(val filter: String) : PeopleCommand()
}