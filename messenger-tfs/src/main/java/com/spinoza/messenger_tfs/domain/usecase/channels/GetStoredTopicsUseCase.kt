package com.spinoza.messenger_tfs.domain.usecase.channels

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.repository.DaoRepository
import javax.inject.Inject

class GetStoredTopicsUseCase @Inject constructor(private val repository: DaoRepository) {

    suspend operator fun invoke(channel: Channel): Result<List<Topic>> {
        return repository.getStoredTopics(channel)
    }
}