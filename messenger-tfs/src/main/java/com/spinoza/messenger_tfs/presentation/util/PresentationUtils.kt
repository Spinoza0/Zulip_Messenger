package com.spinoza.messenger_tfs.presentation.util

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.snackbar.Snackbar
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.di.app.ApplicationComponent
import com.spinoza.messenger_tfs.presentation.feature.app.App

fun Context.getAppComponent(): ApplicationComponent = (this.applicationContext as App).appComponent

fun Context.getThemeColor(attr: Int): Int {
    val typedValue = TypedValue()
    this.theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

fun ShimmerFrameLayout.on() {
    isVisible = true
    startShimmer()
}

fun ShimmerFrameLayout.off() {
    stopShimmer()
    isVisible = false
}

fun RecyclerView.getInstanceState(): Parcelable? {
    return layoutManager?.onSaveInstanceState()
}

fun RecyclerView.restoreInstanceState(state: Parcelable?) {
    layoutManager?.onRestoreInstanceState(state)
}

fun Fragment.showError(text: String) {
    val viewSnackBar = requireActivity().findViewById<View>(android.R.id.content)
    if (viewSnackBar != null) {
        Snackbar.make(viewSnackBar, text, Snackbar.LENGTH_INDEFINITE).apply {
            setAction(android.R.string.ok) { }
            show()
        }
    } else {
        showToast(text)
    }
}

fun Fragment.showToast(text: String) {
    Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
}

fun Fragment.showCheckInternetConnectionDialog(onOkClick: () -> Unit, onCloseClick: () -> Unit) {
    AlertDialog.Builder(requireContext())
        .setMessage(getString(R.string.check_internet_connection))
        .setCancelable(false)
        .setPositiveButton(getString(R.string.ok)) { _, _ ->
            if (isNetworkConnected()) {
                onOkClick()
            } else {
                showCheckInternetConnectionDialog(onOkClick, onCloseClick)
            }
        }
        .setNegativeButton(getString(R.string.close)) { _, _ ->
            onCloseClick()
        }
        .create()
        .show()
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

private fun Fragment.isNetworkConnected(): Boolean {
    val cm =
        requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetwork != null && cm.getNetworkCapabilities(cm.activeNetwork) != null
}

const val DIRECTION_UP = -1
const val DIRECTION_DOWN = 1