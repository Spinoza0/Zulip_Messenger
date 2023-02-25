package com.spinoza.homework_1.presentation.repository

import android.content.Context
import android.provider.ContactsContract
import com.spinoza.homework_1.domain.Contact

class ContactsRepository(private val context: Context) {

    fun requestContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()

        val selection = ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + " > 0"
        val sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME +
                " ASC LIMIT $NUMBER_OF_CONTACTS_TO_RECEIVE"

        val cursorContacts = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            selection,
            null,
            sortOrder
        )
        cursorContacts?.use {
            while (cursorContacts.moveToNext() && contacts.size < NUMBER_OF_CONTACTS_TO_RECEIVE) {
                val name = cursorContacts.getString(
                    cursorContacts.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                )
                val phone = cursorContacts.getString(
                    cursorContacts.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                )
                contacts.add(Contact(name, phone))
            }
        }
        return contacts
    }

    companion object {
        private const val NUMBER_OF_CONTACTS_TO_RECEIVE = 5
    }
}