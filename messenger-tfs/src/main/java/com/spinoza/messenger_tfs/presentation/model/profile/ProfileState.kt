package com.spinoza.messenger_tfs.presentation.model.profile

import com.spinoza.messenger_tfs.domain.model.User

data class ProfileState(
    val isLoading: Boolean = false,
    val user: User? = null,
)