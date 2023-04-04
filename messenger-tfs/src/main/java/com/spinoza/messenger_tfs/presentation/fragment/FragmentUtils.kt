package com.spinoza.messenger_tfs.presentation.fragment

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.spinoza.messenger_tfs.R

fun Fragment.showError(text: String) {
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

fun Fragment.showCheckInternetConnectionDialog(onOkClick: () -> Unit, onCloseClick: () -> Unit) {
    AlertDialog.Builder(requireContext())
        .setMessage(getString(R.string.check_internet_connection))
        .setCancelable(false)
        .setPositiveButton(getString(R.string.button_ok)) { _, _ ->
            if (isNetworkConnected()) {
                onOkClick()
            } else {
                showCheckInternetConnectionDialog(onOkClick, onCloseClick)
            }
        }
        .setNegativeButton(getString(R.string.button_close)) { _, _ ->
            onCloseClick()
        }
        .create()
        .show()
}

fun Fragment.isNetworkConnected(): Boolean {
    val cm =
        requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetwork != null && cm.getNetworkCapabilities(cm.activeNetwork) != null
}

fun Fragment.closeApplication() {
    requireActivity().moveTaskToBack(true)
    requireActivity().finish()
}

@Suppress("deprecation")
inline fun <reified T> Bundle.getParam(paramName: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(paramName, T::class.java)
    } else {
        getParcelable(paramName)
    }
}