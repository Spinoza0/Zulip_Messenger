package com.spinoza.messenger_tfs.presentation.feature.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.spinoza.messenger_tfs.domain.notification.Notificator
import javax.inject.Inject

class NotificatorImpl @Inject constructor(private val context: Context) : Notificator {

    private var notificationId = FIRST_NOTIFICATION_ID
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun createNotificationChannel(
        channelName: String,
        channelId: String,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun showNotification(
        title: String,
        channelId: String,
        iconResId: Int,
        message: String,
    ) {
        val notificationBuilder =
            NotificationCompat.Builder(context, channelId)
                .setSmallIcon(iconResId)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        notificationManager.notify(notificationId++, notificationBuilder.build())
    }

    private companion object {

        const val FIRST_NOTIFICATION_ID = 1
    }
}