package com.spinoza.messenger_tfs.domain.usecase.messages

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import javax.inject.Inject

class SaveAttachmentsUseCase @Inject constructor(private val repository: MessagesRepository) {

    operator fun invoke(urls: List<String>) {
        return repository.saveAttachments(urls)
    }
}