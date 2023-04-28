package com.spinoza.messenger_tfs.domain.usecase.messages

import android.net.Uri
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import javax.inject.Inject

class UploadFileUseCase @Inject constructor(private val repository: MessagesRepository) {

    suspend operator fun invoke(oldMessageText: String, uri: Uri): Result<String> {
        return repository.uploadFile(oldMessageText, uri)
    }
}