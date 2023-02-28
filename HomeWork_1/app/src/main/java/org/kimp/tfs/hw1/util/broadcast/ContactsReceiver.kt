package org.kimp.tfs.hw1.util.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import dagger.hilt.android.AndroidEntryPoint
import org.kimp.tfs.hw1.HomeworkApplication
import org.kimp.tfs.hw1.data.model.ContactInfo
import timber.log.Timber

@AndroidEntryPoint
class ContactsReceiver: BroadcastReceiver() {
    var contactsReceivedListener: ContactsReceivedListener? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.hasExtra("contacts") == true) {
            contactsReceivedListener?.onContactsReceived(
                intent.getParcelableArrayExtra("contacts")
                    ?.map { x -> x as ContactInfo }
                    ?.toTypedArray() ?: emptyArray()
            )
        } else {
            contactsReceivedListener?.onNothingReceived()
        }
    }

    interface ContactsReceivedListener {
        fun onContactsReceived(contacts: Array<ContactInfo>)

        fun onNothingReceived()
    }

    companion object {
        const val ACTION = "org.kimp.tfs.hw1.CONTACTS"
        val INTENT_FILTER = IntentFilter(ACTION)
    }
}
