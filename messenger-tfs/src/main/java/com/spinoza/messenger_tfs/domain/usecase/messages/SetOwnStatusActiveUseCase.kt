package com.spinoza.messenger_tfs.domain.usecase.messages

import com.spinoza.messenger_tfs.domain.repository.UserRepository
import javax.inject.Inject

class SetOwnStatusActiveUseCase @Inject constructor(private val repository: UserRepository) {

    suspend operator fun invoke() {
        repository.setOwnStatusActive()
    }
}