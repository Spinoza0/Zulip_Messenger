package com.spinoza.messenger_tfs.domain.repository

import android.net.Uri

interface AttachmentHandler {

    fun saveAttachments(urls: List<String>)

    suspend fun uploadFile(oldMessageText: String, uri: Uri): Result<String>
}