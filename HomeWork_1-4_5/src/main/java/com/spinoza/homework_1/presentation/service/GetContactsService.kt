package com.spinoza.homework_1.presentation.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.provider.ContactsContract
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.spinoza.homework_1.domain.Contact
import com.spinoza.homework_1.domain.ContactsList
import com.spinoza.homework_1.presentation.broadcastreceiver.ContactsReceiver.Companion.CONTACTS_SERVICE_ACTION
import com.spinoza.homework_1.presentation.broadcastreceiver.ContactsReceiver.Companion.ERROR_SERVICE_ACTION
import com.spinoza.homework_1.presentation.utils.Constants.EXTRA_CONTACTS_LIST
import com.spinoza.homework_1.presentation.utils.Constants.EXTRA_ERROR_TEXT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GetContactsService : Service() {

    private val localBroadcastManager by lazy {
        LocalBroadcastManager.getInstance(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoroutineScope(Dispatchers.Default).launch {
            val resultIntent = runCatching {
                val contactsList = ContactsList(requestContacts())
                Intent(CONTACTS_SERVICE_ACTION).putExtra(EXTRA_CONTACTS_LIST, contactsList)
            }.getOrElse {
                Intent(ERROR_SERVICE_ACTION).putExtra(EXTRA_ERROR_TEXT, it.localizedMessage)
            }
            localBroadcastManager.sendBroadcast(resultIntent)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun requestContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()

        val selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " > 0"
        val sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " ASC LIMIT 10"

        val cursorContacts = contentResolver.query(
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
        val cursorPhones = contentResolver.query(
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val NUMBER_OF_CONTACTS_TO_RECEIVE = 5
        fun newIntent(context: Context) = Intent(context, GetContactsService::class.java)
    }
}