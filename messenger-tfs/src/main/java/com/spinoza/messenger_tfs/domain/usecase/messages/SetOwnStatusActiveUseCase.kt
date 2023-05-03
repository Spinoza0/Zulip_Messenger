package com.spinoza.messenger_tfs.domain.usecase.messages

import com.spinoza.messenger_tfs.domain.repository.WebRepository
import javax.inject.Inject

class SetOwnStatusActiveUseCase @Inject constructor(private val repository: WebRepository) {

    suspend operator fun invoke() {
        repository.setOwnStatusActive()
    }
}