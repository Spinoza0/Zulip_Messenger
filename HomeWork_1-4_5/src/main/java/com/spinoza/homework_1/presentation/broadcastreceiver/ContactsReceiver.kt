package com.spinoza.homework_1.presentation.broadcastreceiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class ContactsReceiver(
    private val sendResultToConsumer: ((Int, Intent) -> Unit),
) {

    private var localBroadcastManager: LocalBroadcastManager? = null

    private val contactsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                CONTACTS_SERVICE_ACTION -> {
                    sendResultToConsumer.invoke(Activity.RESULT_OK, intent)
                }
                ERROR_SERVICE_ACTION -> {
                    sendResultToConsumer.invoke(Activity.RESULT_CANCELED, intent)
                }
            }
        }
    }

    fun register(context: Context) {
        val filter = IntentFilter().apply {
            addAction(CONTACTS_SERVICE_ACTION)
            addAction(ERROR_SERVICE_ACTION)
        }
        localBroadcastManager = LocalBroadcastManager.getInstance(context)
        localBroadcastManager?.registerReceiver(contactsReceiver, filter)
    }

    fun unregister() {
        localBroadcastManager?.unregisterReceiver(contactsReceiver)
        localBroadcastManager = null
    }

    companion object {
        const val CONTACTS_SERVICE_ACTION = "contacts"
        const val ERROR_SERVICE_ACTION = "error"
    }
}