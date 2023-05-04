package com.spinoza.messenger_tfs.domain.usecase.messages

import android.content.Context
import com.spinoza.messenger_tfs.domain.network.AttachmentHandler
import javax.inject.Inject

class SaveAttachmentsUseCase @Inject constructor(private val attachmentHandler: AttachmentHandler) {

    suspend operator fun invoke(context: Context, urls: List<String>): Map<String, Boolean> {
        return attachmentHandler.saveAttachments(context, urls)
    }
}