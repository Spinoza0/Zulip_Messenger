package com.spinoza.messenger_tfs.data.network

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.spinoza.messenger_tfs.BuildConfig
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.network.AuthorizationStorage
import okhttp3.Credentials
import javax.inject.Inject

class AuthorizationStorageImpl @Inject constructor(context: Context) : AuthorizationStorage {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private var headerValue: String = EMPTY_STRING
    private var userId: Long = User.UNDEFINED_ID

    private var editor = sharedPreferences.edit()

    override fun isUserLoggedIn(): Boolean {
        return userId != User.UNDEFINED_ID && headerValue.isNotBlank()
    }

    override fun isAuthorizationDataExisted(): Boolean {
        return getApiKey().isNotBlank() && getEmail().isNotBlank() && getPassword().isNotBlank()
    }

    override fun makeAuthHeader(email: String, apiKey: String): String {
        val newApiKey = apiKey.ifBlank { getApiKey() }
        headerValue = if (newApiKey.isBlank()) EMPTY_STRING else Credentials.basic(email, newApiKey)
        return headerValue
    }

    override fun getAuthHeaderTitle(): String = HEADER_AUTHORIZATION

    override fun getAuthHeaderValue(): String {
        return headerValue
    }

    override fun getUserId(): Long {
        return userId
    }

    override fun getEmail(): String {
        return sharedPreferences.getString(PARAM_EMAIL, EMPTY_STRING) ?: EMPTY_STRING
    }

    override fun getPassword(): String {
        return sharedPreferences.getString(PARAM_PASSWORD, EMPTY_STRING) ?: EMPTY_STRING
    }

    override fun saveData(userId: Long, email: String, password: String, apiKey: String) {
        this.userId = userId
        editor.putString(PARAM_EMAIL, email)
        editor.putString(PARAM_PASSWORD, password)
        if (apiKey.isNotBlank()) {
            editor.putString(PARAM_API_KEY, apiKey)
        }
        editor.apply()
    }

    override fun deleteData() {
        userId = User.UNDEFINED_ID
        headerValue = EMPTY_STRING
        editor.clear().apply()
    }

    private fun getApiKey(): String {
        return sharedPreferences.getString(PARAM_API_KEY, EMPTY_STRING) ?: EMPTY_STRING
    }

    private companion object {

        const val FILE_NAME = BuildConfig.SHAREDPREF
        const val HEADER_AUTHORIZATION = "Authorization"
        const val PARAM_API_KEY = "apiKey"
        const val PARAM_EMAIL = "email"
        const val PARAM_PASSWORD = "password"
        const val EMPTY_STRING = ""
    }
}