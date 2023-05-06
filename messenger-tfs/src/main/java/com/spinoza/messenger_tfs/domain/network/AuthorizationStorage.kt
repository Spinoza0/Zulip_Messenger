package com.spinoza.messenger_tfs.domain.network

interface AuthorizationStorage {

    fun isUserLoggedIn(): Boolean

    fun isAuthorizationDataExisted(): Boolean

    fun makeAuthHeader(email: String, apiKey: String = EMPTY_STRING): String

    fun getAuthHeaderTitle(): String

    fun getAuthHeaderValue(): String

    fun getUserId(): Long

    fun isAdmin(): Boolean

    fun getEmail(): String

    fun getPassword(): String

    fun saveData(
        userId: Long,
        isAdmin: Boolean,
        email: String,
        password: String,
        apiKey: String = EMPTY_STRING,
    )

    fun deleteData()

    private companion object {

        const val EMPTY_STRING = ""
    }
}