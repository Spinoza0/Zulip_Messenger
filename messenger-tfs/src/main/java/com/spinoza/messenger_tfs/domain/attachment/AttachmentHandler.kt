package com.spinoza.messenger_tfs.domain.attachment

import android.content.Context
import android.net.Uri

interface AttachmentHandler {

    suspend fun saveAttachments(urls: List<String>): Map<String, Boolean>

    suspend fun uploadFile(context: Context, oldMessageText: String, uri: Uri): Result<String>
}