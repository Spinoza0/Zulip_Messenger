package com.spinoza.messenger_tfs.domain.network

import android.content.Context
import android.net.Uri
import com.spinoza.messenger_tfs.domain.model.UploadedFileInfo

interface AttachmentHandler {

    suspend fun saveAttachments(urls: List<String>): Map<String, Boolean>

    suspend fun uploadFile(context: Context, uri: Uri): Result<UploadedFileInfo>
}