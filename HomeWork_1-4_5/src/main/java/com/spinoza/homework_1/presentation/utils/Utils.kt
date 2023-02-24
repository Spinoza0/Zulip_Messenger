package com.spinoza.homework_1.domain

import android.content.Intent
import android.os.Build
import com.spinoza.homework_1.presentation.utils.Constants

fun getContactsListFromIntent(intent: Intent): ContactsList {
    val contactsList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getParcelableExtra(Constants.EXTRA_CONTACTS_LIST, ContactsList::class.java)
    } else {
        @Suppress("deprecation")
        intent.getParcelableExtra<ContactsList>(Constants.EXTRA_CONTACTS_LIST) as ContactsList
    }

    return contactsList ?: throw RuntimeException("Parameter ContactsList not found in intent")
}