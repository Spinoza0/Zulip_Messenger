package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import com.spinoza.messenger_tfs.domain.Cursor

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

fun drawView(view: View, cursor: Cursor) {
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

fun Context.getThemeColor(attr: Int): Int {
    val typedValue = TypedValue()
    this.theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

fun Bitmap.getRounded(size: Float): Bitmap {
    val result = Bitmap.createBitmap(size.toInt(), size.toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val path = Path()
    path.addCircle((size - 1) / 2, (size - 1) / 2, size / 2, Path.Direction.CCW)
    canvas.clipPath(path)
    canvas.drawBitmap(
        this,
        Rect(0, 0, this.width, this.height),
        Rect(0, 0, size.toInt(), size.toInt()), null
    )
    return result
}
