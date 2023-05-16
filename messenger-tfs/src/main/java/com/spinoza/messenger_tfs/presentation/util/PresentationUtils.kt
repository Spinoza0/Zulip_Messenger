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
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.di.app.ApplicationComponent
import com.spinoza.messenger_tfs.domain.util.EMPTY_STRING
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

fun Fragment.showError(text: String, errorData: String) {
    showConfirmationDialog(
        title = text,
        message = getErrorMessage(this, errorData),
        positiveButtonTitleResId = R.string.ok,
        onPositiveClickCallback = {}
    )
}

fun Fragment.showToast(text: String) {
    Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
}

fun Fragment.showCheckInternetConnectionDialog(
    errorData: String,
    onOkClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    showConfirmationDialog(
        title = getString(R.string.check_internet_connection),
        message = getErrorMessage(this, errorData),
        positiveButtonTitleResId = R.string.ok,
        negativeButtonTitleResId = R.string.close_screen,
        onPositiveClickCallback = {
            if (isNetworkConnected()) {
                onOkClick()
            } else {
                showCheckInternetConnectionDialog(errorData, onOkClick, onCloseClick)
            }
        }
    ) { onCloseClick() }
}

fun Fragment.showConfirmationDialog(
    onPositiveClickCallback: () -> Unit,
    positiveButtonTitleResId: Int = R.string.yes,
    negativeButtonTitleResId: Int? = R.string.no,
    title: String = EMPTY_STRING,
    message: String = EMPTY_STRING,
    view: View? = null,
    onNegativeClickCallback: (() -> Unit)? = null,
) {
    val dialogBuilder = AlertDialog.Builder(requireContext())
        .setCancelable(false)
        .setPositiveButton(getString(positiveButtonTitleResId)) { _, _ ->
            onPositiveClickCallback()
        }
    if (negativeButtonTitleResId != null) dialogBuilder.setNegativeButton(
        getString(negativeButtonTitleResId)
    ) { _, _ ->
        onNegativeClickCallback?.invoke()
    }
    if (title.isNotEmpty()) dialogBuilder.setTitle(title)
    if (message.isNotEmpty()) dialogBuilder.setMessage(message)
    if (view != null) dialogBuilder.setView(view)
    dialogBuilder.create().show()
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

private fun getErrorMessage(fragment: Fragment, errorData: String) = when {
    errorData.contains(ERROR_BAD_REQUEST) ->
        fragment.getString(R.string.error_bad_request)

    errorData.contains(ERROR_UNAUTHORIZED) ->
        fragment.getString(R.string.error_unauthorized)

    errorData.contains(ERROR_FORBIDDEN) ->
        fragment.getString(R.string.error_forbidden)

    errorData.contains(ERROR_URL_NOT_FOUND) ->
        fragment.getString(R.string.error_url_not_found)

    errorData.contains(ERROR_TOO_MANY_ATTEMPTS) ->
        fragment.getString(R.string.error_too_many_attempts)

    errorData.contains(ERROR_INTERNAL_SERVER_PROBLEM) ->
        fragment.getString(R.string.error_internal_server_problem)

    else -> errorData
}

const val DIRECTION_UP = -1
const val DIRECTION_DOWN = 1
private const val ERROR_BAD_REQUEST = "HTTP 400"
private const val ERROR_UNAUTHORIZED = "HTTP 401"
private const val ERROR_FORBIDDEN = "HTTP 403"
private const val ERROR_URL_NOT_FOUND = "HTTP 404"
private const val ERROR_TOO_MANY_ATTEMPTS = "HTTP 429"
private const val ERROR_INTERNAL_SERVER_PROBLEM = "HTTP 500"