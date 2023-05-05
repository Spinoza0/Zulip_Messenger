package com.spinoza.messenger_tfs.stub

import com.spinoza.messenger_tfs.domain.usermanager.UserManager

class UserManagerStub : UserManager {

    override fun saveData(apiKey: String, email: String, password: String) {}

    override fun getApiKey(): String = "key"

    override fun getEmail(): String = "email"

    override fun getPassword(): String = "password"

    override fun deleteData() {}
}