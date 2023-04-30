package com.spinoza.messenger_tfs.stub

import android.net.Uri
import com.spinoza.messenger_tfs.domain.attachment.AttachmentHandler

class AttachmentHandlerStub : AttachmentHandler {

    override fun saveAttachments(urls: List<String>) {}

    override suspend fun uploadFile(oldMessageText: String, uri: Uri): Result<String> {
        return Result.failure(RuntimeException("test failure"))
    }
}