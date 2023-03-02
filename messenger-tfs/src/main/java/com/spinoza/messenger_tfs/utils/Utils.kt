package com.spinoza.messenger_tfs

import android.util.TypedValue
import android.view.View

fun Float.spToPx(view: View): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        view.resources.displayMetrics
    )
}

fun Float.dpToPx(view: View): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        view.resources.displayMetrics
    )
}