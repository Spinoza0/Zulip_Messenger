package com.spinoza.messenger_tfs.domain.repository

import android.net.Uri

interface AttachmentHandler {

    suspend fun saveAttachments(urls: List<String>): Map<String, Boolean>

    suspend fun uploadFile(oldMessageText: String, uri: Uri): Result<String>
}