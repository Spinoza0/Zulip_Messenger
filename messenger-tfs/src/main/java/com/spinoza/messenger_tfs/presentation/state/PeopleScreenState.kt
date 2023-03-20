package com.spinoza.messenger_tfs.presentation.state

import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

sealed class PeopleScreenState {

    object Loading : PeopleScreenState()

    class Users(val value: List<User>) : PeopleScreenState()

    class Error(val value: RepositoryResult) : PeopleScreenState()
}