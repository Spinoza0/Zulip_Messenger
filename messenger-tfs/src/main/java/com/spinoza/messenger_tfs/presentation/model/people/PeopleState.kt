package com.spinoza.messenger_tfs.presentation.model.people

import com.spinoza.messenger_tfs.domain.model.User

data class PeopleState(
    val isLoading: Boolean = false,
    val users: List<User>? = null,
    val filter: String = "",
)