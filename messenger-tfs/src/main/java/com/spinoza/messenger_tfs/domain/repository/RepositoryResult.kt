package com.spinoza.messenger_tfs.domain.repository

class RepositoryResult(
    val type: Type,
    val text: String = "",
) {
    enum class Type {
        SUCCESS,
        ERROR_USER_WITH_ID_NOT_FOUND,
        ERROR_MESSAGE_WITH_ID_NOT_FOUND
    }
}