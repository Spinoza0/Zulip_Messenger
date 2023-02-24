package com.spinoza.homework_1.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class GetContactsResult : Parcelable {
    class Success(val contacts: List<Contact>) : GetContactsResult()
    class Error(val message: String) : GetContactsResult()
}