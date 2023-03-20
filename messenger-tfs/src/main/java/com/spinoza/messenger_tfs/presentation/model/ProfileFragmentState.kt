package com.spinoza.messenger_tfs.presentation.model

import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

sealed class ProfileFragmentState {

    object Loading : ProfileFragmentState()

    class UserData(val value: User) : ProfileFragmentState()

    class Error(val value: RepositoryResult) : ProfileFragmentState()
}