package com.spinoza.messenger_tfs.domain.usecase.messages

import com.spinoza.messenger_tfs.domain.repository.MessengerRepository
import javax.inject.Inject

class SaveAttachmentsUseCase @Inject constructor(private val repository: MessengerRepository) {

    suspend operator fun invoke(urls: List<String>): Map<String, Boolean> {
        return repository.saveAttachments(urls)
    }
}