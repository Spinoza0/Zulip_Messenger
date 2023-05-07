package com.spinoza.messenger_tfs.stub

import com.spinoza.messenger_tfs.domain.network.AuthorizationStorage

class AuthorizationStorageStub : AuthorizationStorage {

    override fun isUserLoggedIn(): Boolean = true

    override fun isAdmin(): Boolean = false

    override fun isAuthorizationDataExisted(): Boolean = true

    override fun makeAuthHeader(email: String, apiKey: String): String = "AuthHeader"

    override fun getAuthHeaderTitle(): String = "Title"

    override fun getAuthHeaderValue(): String = "Value"

    override fun getUserId(): Long = 604180

    override fun saveData(
        userId: Long,
        isAdmin: Boolean,
        email: String,
        password: String,
        apiKey: String,
    ) {
    }

    override fun getEmail(): String = "email"

    override fun getPassword(): String = "password"

    override fun deleteData() {}
}