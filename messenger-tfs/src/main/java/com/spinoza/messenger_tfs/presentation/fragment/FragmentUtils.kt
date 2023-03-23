package com.spinoza.messenger_tfs.presentation.fragment

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

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

@Suppress("deprecation")
inline fun <reified T> Bundle.getParam(paramName: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(paramName, T::class.java)
    } else {
        getParcelable(paramName)
    }
}