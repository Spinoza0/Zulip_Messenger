package com.spinoza.messenger_tfs.presentation.util

import android.content.Context
import android.os.Parcelable
import android.util.TypedValue
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout

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