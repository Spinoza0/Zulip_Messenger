package com.spinoza.homework_1.presentation.repository

import android.content.Context
import android.provider.ContactsContract
import com.spinoza.homework_1.domain.Contact

class ContactsRepository(private val context: Context) {

    fun requestContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()

        val selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " > 0"
        val sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " ASC LIMIT 10"

        val cursorContacts = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            selection,
            null,
            sortOrder
        )
        cursorContacts?.use {
            while (cursorContacts.moveToNext() && contacts.size < NUMBER_OF_CONTACTS_TO_RECEIVE) {
                val id = cursorContacts.getString(
                    cursorContacts.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
                )
                val name = cursorContacts.getString(
                    cursorContacts.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
                )
                val phone = getPhoneNumber(id)
                contacts.add(Contact(name, phone))
            }
        }
        return contacts
    }

    private fun getPhoneNumber(id: String): String {
        var result = ""
        val cursorPhones = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
            arrayOf<String>(id),
            null
        )
        cursorPhones?.use {
            if (cursorPhones.moveToNext()) {
                result = cursorPhones.getString(
                    cursorPhones.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                )
            }
        }
        return result
    }

    companion object {
        private const val NUMBER_OF_CONTACTS_TO_RECEIVE = 5
    }
}