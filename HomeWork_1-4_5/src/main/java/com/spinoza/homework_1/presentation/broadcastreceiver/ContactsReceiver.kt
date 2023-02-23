package com.spinoza.homework_1.presentation.broadcastreceiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.spinoza.homework_1.domain.getContactsListFromIntent
import com.spinoza.homework_1.presentation.utils.Constants
import com.spinoza.homework_1.presentation.utils.Constants.Companion.CONTACTS_SERVICE_ACTION
import com.spinoza.homework_1.presentation.utils.Constants.Companion.ERROR_SERVICE_ACTION
import com.spinoza.homework_1.presentation.utils.Constants.Companion.EXTRA_ERROR_TEXT

class ContactsReceiver(
    private val sendResultToConsumer: ((Int, Intent) -> Unit),
) {

    private var localBroadcastManager: LocalBroadcastManager? = null

    private val contactsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                CONTACTS_SERVICE_ACTION -> {
                    val contacts = getContactsListFromIntent(intent)
                    Intent().apply {
                        putExtra(Constants.EXTRA_CONTACTS_LIST, contacts)
                        sendResultToConsumer.invoke(Activity.RESULT_OK, this)
                    }
                }
                ERROR_SERVICE_ACTION -> {
                    Intent().apply {
                        putExtra(
                            EXTRA_ERROR_TEXT,
                            intent.getStringExtra(EXTRA_ERROR_TEXT)
                        )
                        sendResultToConsumer.invoke(Activity.RESULT_CANCELED, this)
                    }
                }
            }
        }
    }

    fun register(context: Context) {
        if (localBroadcastManager == null) {
            IntentFilter().apply {
                addAction(CONTACTS_SERVICE_ACTION)
                addAction(ERROR_SERVICE_ACTION)
                localBroadcastManager = LocalBroadcastManager.getInstance(context)
                localBroadcastManager?.registerReceiver(contactsReceiver, this)
            }
        }
    }

    fun unregister() {
        localBroadcastManager?.unregisterReceiver(contactsReceiver)
        localBroadcastManager = null
    }
}