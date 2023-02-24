package com.spinoza.homework_1.presentation.utils

import android.content.Intent
import android.os.Build
import com.spinoza.homework_1.domain.Contact

fun getContactsListFromIntent(intent: Intent): List<Contact> {
    val contactsList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getParcelableArrayListExtra(Constants.EXTRA_CONTACTS_LIST, Contact::class.java)
    } else {
        @Suppress("deprecation")
        intent.getParcelableArrayListExtra(Constants.EXTRA_CONTACTS_LIST)
    }

    return contactsList ?: throw RuntimeException("Parameter ContactsList not found in intent")
}