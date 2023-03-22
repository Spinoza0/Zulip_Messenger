package com.spinoza.messenger_tfs.presentation.fragment

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult

fun Fragment.showError(result: RepositoryResult) {
    val text = when (result.type) {
        RepositoryResult.Type.ERROR_MESSAGE_WITH_ID_NOT_FOUND -> {
            String.format(getString(R.string.error_message_not_found), result.text)
        }
        RepositoryResult.Type.ERROR_USER_WITH_ID_NOT_FOUND -> {
            String.format(getString(R.string.error_user_not_found), result.text)
        }
        else -> result.text
    }
    val viewSnackBar = requireActivity().findViewById<View>(android.R.id.content)
    if (viewSnackBar != null) {
        Snackbar.make(viewSnackBar, text, Snackbar.LENGTH_INDEFINITE).apply {
            setAction(android.R.string.ok) { }
            show()
        }
    } else {
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
    }
}

@Suppress("deprecation")
inline fun <reified T> Bundle.getParam(paramName: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(paramName, T::class.java)
    } else {
        getParcelable(paramName)
    }
}