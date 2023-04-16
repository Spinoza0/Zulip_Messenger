package com.spinoza.messenger_tfs.presentation.feature.profile.model

import android.os.Parcelable
import com.spinoza.messenger_tfs.domain.model.User
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileScreenState(
    val isLoading: Boolean = false,
    val user: User? = null,
) : Parcelable