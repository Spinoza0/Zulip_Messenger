package com.spinoza.messenger_tfs.domain.usecase.messages

import android.content.Context
import android.net.Uri
import com.spinoza.messenger_tfs.domain.network.AttachmentHandler
import javax.inject.Inject

class UploadFileUseCase @Inject constructor(private val attachmentHandler: AttachmentHandler) {

    suspend operator fun invoke(context: Context, uri: Uri): Result<Pair<String, String>> {
        return attachmentHandler.uploadFile(context, uri)
    }
}