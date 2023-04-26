package com.spinoza.messenger_tfs.domain.usecase

import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import java.io.InputStream
import javax.inject.Inject

class UploadFileUseCase @Inject constructor(private val repository: MessagesRepository) {

    suspend operator fun invoke(name: String, inputStream: InputStream): Result<String> {
        return repository.uploadFile(name, inputStream)
    }
}