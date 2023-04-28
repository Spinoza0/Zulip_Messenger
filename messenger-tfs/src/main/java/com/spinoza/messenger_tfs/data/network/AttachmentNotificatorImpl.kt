package com.spinoza.messenger_tfs.data.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.repository.AttachmentNotificator
import javax.inject.Inject

class AttachmentNotificatorImpl @Inject constructor(private val context: Context) :
    AttachmentNotificator {

    private var notificationId = FIRST_NOTIFICATION_ID
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    override fun showNotification(message: String) {
        val notificationBuilder =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_download_complete)
                .setContentTitle(context.getString(R.string.download_complete))
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        notificationManager.notify(notificationId++, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private companion object {

        const val FIRST_NOTIFICATION_ID = 1
        const val CHANNEL_NAME = "Downloads"
        const val CHANNEL_ID = "downloads_channel"
    }
}