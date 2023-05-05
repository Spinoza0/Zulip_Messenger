package com.spinoza.messenger_tfs.domain.usecase.messages

import com.spinoza.messenger_tfs.domain.network.AuthorizationStorage
import javax.inject.Inject

class GetOwnUserIdUseCase @Inject constructor(private val authorizationStorage: AuthorizationStorage) {

    operator fun invoke(): Long {
        return authorizationStorage.getUserId()
    }
}