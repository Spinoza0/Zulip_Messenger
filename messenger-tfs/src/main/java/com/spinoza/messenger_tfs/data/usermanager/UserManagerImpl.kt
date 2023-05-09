package com.spinoza.messenger_tfs.data.usermanager

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.spinoza.messenger_tfs.BuildConfig
import com.spinoza.messenger_tfs.domain.usermanager.UserManager
import javax.inject.Inject

class UserManagerImpl @Inject constructor(context: Context) : UserManager {

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

    private var editor = sharedPreferences.edit()

    override fun saveData(apiKey: String, email: String, password: String) {
        editor
            .putString(PARAM_API_KEY, apiKey)
            .putString(PARAM_EMAIL, email)
            .putString(PARAM_PASSWORD, password)
            .apply()
    }

    override fun getApiKey(): String {
        return sharedPreferences.getString(PARAM_API_KEY, NO_VALUE) ?: NO_VALUE
    }

    override fun getEmail(): String {
        return sharedPreferences.getString(PARAM_EMAIL, NO_VALUE) ?: NO_VALUE
    }

    override fun getPassword(): String {
        return sharedPreferences.getString(PARAM_PASSWORD, NO_VALUE) ?: NO_VALUE
    }

    override fun deleteData() {
        editor.clear().apply()
    }

    private companion object {

        const val FILE_NAME = BuildConfig.SHAREDPREF
        const val PARAM_API_KEY = "apiKey"
        const val PARAM_EMAIL = "email"
        const val PARAM_PASSWORD = "password"
        const val NO_VALUE = ""
    }
}