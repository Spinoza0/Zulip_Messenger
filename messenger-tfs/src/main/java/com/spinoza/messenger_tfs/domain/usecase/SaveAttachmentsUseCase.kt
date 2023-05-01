package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import javax.inject.Inject

class SaveAttachmentsUseCase @Inject constructor(private val repository: MessagesRepository) {

    suspend operator fun invoke(urls: List<String>): Map<String, Boolean> {
        return repository.saveAttachments(urls)
    }
}