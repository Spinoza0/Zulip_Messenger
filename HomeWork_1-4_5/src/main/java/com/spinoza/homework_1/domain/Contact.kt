package com.spinoza.homework_1.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contact(
    val name: String,
    val phone: String,
): Parcelable