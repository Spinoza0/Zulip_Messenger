package com.spinoza.homework_1.presentation.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.spinoza.homework_1.domain.GetContactsResult
import com.spinoza.homework_1.presentation.broadcastreceiver.ContactsReceiver.Companion.CONTACTS_SERVICE_ACTION
import com.spinoza.homework_1.presentation.broadcastreceiver.ContactsReceiver.Companion.ERROR_SERVICE_ACTION
import com.spinoza.homework_1.presentation.repository.ContactsRepository
import com.spinoza.homework_1.presentation.utils.Constants.EXTRA_RESULT
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
                val contactsRepository = ContactsRepository(this@GetContactsService)
                val contacts = contactsRepository.requestContacts()
                Intent(CONTACTS_SERVICE_ACTION).putExtra(
                    EXTRA_RESULT,
                    GetContactsResult.Success(contacts)
                )
            }.getOrElse {
                Intent(ERROR_SERVICE_ACTION).putExtra(
                    EXTRA_RESULT,
                    GetContactsResult.Error(it.localizedMessage ?: it.message ?: it.toString())
                )
            }
            localBroadcastManager.sendBroadcast(resultIntent)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, GetContactsService::class.java)
    }
}