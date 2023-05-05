package com.spinoza.messenger_tfs.stub

import com.spinoza.messenger_tfs.domain.network.AuthorizationStorage

class AuthorizationStorageStub : AuthorizationStorage {

    override fun makeAuthHeader(email: String, apiKey: String): String = "AuthHeader"

    override fun getAuthHeaderTitle(): String = "Title"

    override fun getAuthHeaderValue(): String = "Value"

    override fun getUserId(): Long = 0

    override fun saveData(userId: Long, email: String, password: String, apiKey: String) {}

    override fun getApiKey(): String = "key"

    override fun getEmail(): String = "email"

    override fun getPassword(): String = "password"

    override fun deleteData() {}
}