package com.spinoza.messenger_tfs.domain.repository

import android.content.Context
import android.net.Uri

interface AttachmentHandler {

    fun downloadAndNotify(context: Context, urls: List<String>)

    suspend fun uploadFile(oldMessageText: String, uri: Uri): Result<String>
}