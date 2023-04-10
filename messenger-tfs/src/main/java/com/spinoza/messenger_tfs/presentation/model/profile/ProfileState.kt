package com.spinoza.messenger_tfs.presentation.model.profile

import android.os.Parcelable
import com.spinoza.messenger_tfs.domain.model.User
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileState(
    val isLoading: Boolean = false,
    val user: User? = null,
) : Parcelable