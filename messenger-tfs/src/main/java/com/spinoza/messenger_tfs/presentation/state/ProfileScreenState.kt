package com.spinoza.messenger_tfs.presentation.state

import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

sealed class ProfileScreenState {

    object Loading : ProfileScreenState()

    class UserData(val value: User) : ProfileScreenState()

    class Error(val value: RepositoryResult) : ProfileScreenState()
}