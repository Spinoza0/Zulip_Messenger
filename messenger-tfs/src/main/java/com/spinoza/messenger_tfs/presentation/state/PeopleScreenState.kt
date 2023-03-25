package com.spinoza.messenger_tfs.presentation.state

import com.spinoza.messenger_tfs.domain.model.User

sealed class PeopleScreenState {

    object Loading : PeopleScreenState()

    class Users(val value: List<User>) : PeopleScreenState()

    class Filter(val value: String) : PeopleScreenState()
}