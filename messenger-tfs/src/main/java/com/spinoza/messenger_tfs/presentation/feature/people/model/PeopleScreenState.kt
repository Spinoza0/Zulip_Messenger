package com.spinoza.messenger_tfs.presentation.feature.people.model

import com.spinoza.messenger_tfs.domain.model.User

data class PeopleScreenState(
    val isLoading: Boolean = false,
    val users: List<User>? = null,
    val filter: String = "",
)