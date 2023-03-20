package com.spinoza.messenger_tfs.presentation.model

import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

sealed class ItemPeopleFragmentState {

    object Loading : ItemPeopleFragmentState()

    class Users(val value: List<User>) : ItemPeopleFragmentState()

    class Error(val value: RepositoryResult) : ItemPeopleFragmentState()
}