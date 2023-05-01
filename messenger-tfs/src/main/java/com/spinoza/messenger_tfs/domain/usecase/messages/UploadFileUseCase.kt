package com.spinoza.messenger_tfs.domain.usecase.messages

import android.net.Uri
import com.spinoza.messenger_tfs.domain.attachment.AttachmentHandler
import javax.inject.Inject

class UploadFileUseCase @Inject constructor(private val attachmentHandler: AttachmentHandler) {

    suspend operator fun invoke(oldMessageText: String, uri: Uri): Result<String> {
        return attachmentHandler.uploadFile(oldMessageText, uri)
    }
}