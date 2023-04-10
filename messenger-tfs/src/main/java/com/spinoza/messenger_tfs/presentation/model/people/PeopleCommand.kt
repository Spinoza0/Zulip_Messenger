package com.spinoza.messenger_tfs.presentation.model.people

sealed class PeopleCommand {

    class Load(val filter: String) : PeopleCommand()

    class Filter(val filter: String) : PeopleCommand()
}