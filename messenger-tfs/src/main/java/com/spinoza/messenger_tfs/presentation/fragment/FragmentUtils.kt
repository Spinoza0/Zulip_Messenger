package com.spinoza.messenger_tfs.presentation.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

fun Context.showError(result: RepositoryResult) {
    val text = when (result.type) {
        RepositoryResult.Type.ERROR_MESSAGE_WITH_ID_NOT_FOUND -> {
            String.format(getString(R.string.error_message_not_found), result.text)
        }
        RepositoryResult.Type.ERROR_USER_WITH_ID_NOT_FOUND -> {
            String.format(getString(R.string.error_user_not_found), result.text)
        }
        else -> result.text
    }
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}

@Suppress("deprecation")
inline fun <reified T> Bundle.getParam(paramName: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(paramName, T::class.java)
    } else {
        getParcelable(paramName)
    }
}