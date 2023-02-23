package com.spinoza.homework_1.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContactsList(val value: List<Contact>): Parcelable