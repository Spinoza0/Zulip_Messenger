package com.spinoza.messenger_tfs.stub

import android.content.Context
import android.net.Uri
import com.spinoza.messenger_tfs.domain.network.AttachmentHandler

class AttachmentHandlerStub : AttachmentHandler {

    override suspend fun saveAttachments(urls: List<String>): Map<String, Boolean> {
        return emptyMap()
    }

    override suspend fun uploadFile(
        context: Context,
        uri: Uri,
    ): Result<Pair<String, String>> {
        return Result.failure(RuntimeException("test failure"))
    }
}