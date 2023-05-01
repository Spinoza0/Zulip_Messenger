package com.spinoza.messenger_tfs.domain.notification

interface Notificator {

    fun createNotificationChannel(channelName: String, channelId: String)

    fun showNotification(title: String, channelId: String, iconResId: Int, message: String)
}