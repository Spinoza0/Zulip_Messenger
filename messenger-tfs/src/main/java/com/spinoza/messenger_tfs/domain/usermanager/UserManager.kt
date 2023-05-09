package com.spinoza.messenger_tfs.domain.usermanager

interface UserManager {

    fun saveData(apiKey: String, email: String, password: String)

    fun getApiKey(): String

    fun getEmail(): String

    fun getPassword(): String

    fun deleteData()
}