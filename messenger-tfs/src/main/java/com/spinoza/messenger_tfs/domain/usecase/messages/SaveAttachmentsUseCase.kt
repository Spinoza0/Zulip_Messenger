package com.spinoza.messenger_tfs.domain.usecase.messages

import com.spinoza.messenger_tfs.domain.attachment.AttachmentHandler
import javax.inject.Inject

class SaveAttachmentsUseCase @Inject constructor(private val attachmentHandler: AttachmentHandler) {

    suspend operator fun invoke(urls: List<String>): Map<String, Boolean> {
        return attachmentHandler.saveAttachments(urls)
    }
}