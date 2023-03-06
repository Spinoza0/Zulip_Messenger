package com.spinoza.messenger_tfs.presentation.ui

import android.util.TypedValue
import android.view.View
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import com.spinoza.messenger_tfs.domain.CursorXY

fun Float.spToPx(view: View) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP,
    this,
    view.resources.displayMetrics
)

fun Float.dpToPx(view: View) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this,
    view.resources.displayMetrics
)

fun drawView(view: View, cursor: CursorXY) {
    cursor.right(view.marginLeft)
    cursor.down(view.marginTop)
    view.layout(
        cursor.x,
        cursor.y,
        cursor.x + view.measuredWidth,
        cursor.y + view.measuredHeight
    )
    cursor.left(view.marginLeft)
    cursor.up(view.marginTop)
}
