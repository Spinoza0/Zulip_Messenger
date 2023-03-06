package org.kimp.tfs.hw1.util.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.database.Cursor
import android.os.IBinder
import android.provider.ContactsContract
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.hilt.android.AndroidEntryPoint
import org.kimp.tfs.hw1.HomeworkApplication
import org.kimp.tfs.hw1.data.model.ContactInfo
import org.kimp.tfs.hw1.util.broadcast.ContactsReceiver
import timber.log.Timber
import kotlin.concurrent.thread

@AndroidEntryPoint
class ContactsLoaderService: Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.tag(HomeworkApplication.TAG)
            .i("Started ContactsLoaderService")

        thread {
            val loadedData = ArrayList<ContactInfo>()

            createContactsCursor()?.use { cursor ->
                while (cursor.moveToNext()) {
                    val contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                    val displayName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    val phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))

                    loadedData.add(ContactInfo(contactId, displayName, phoneNumber))
                }
            }

            Timber.tag(HomeworkApplication.TAG)
                .i("Loaded ${loadedData.size} contacts")

            Intent(ContactsReceiver.ACTION)
                .putExtra("contacts", loadedData.toTypedArray())
                .also { intent ->
                    LocalBroadcastManager.getInstance(this)
                        .sendBroadcast(intent)
                }
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createContactsCursor(): Cursor? {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val selection = "${ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER} > 0 AND " +
                "LENGTH(${ContactsContract.CommonDataKinds.Phone.NUMBER}) > 0"

        return contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            null,
            null
        )
    }
}
