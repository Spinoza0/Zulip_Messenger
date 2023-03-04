package com.spinoza.messenger_tfs

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect
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

fun getThemeColor(context: Context, attr: Int): Int {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

fun getRoundImage(image: Bitmap, size: Float): Bitmap {
    val result = Bitmap.createBitmap(size.toInt(), size.toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val path = Path()
    path.addCircle((size - 1) / 2, (size - 1) / 2, size / 2, Path.Direction.CCW)
    canvas.clipPath(path)
    canvas.drawBitmap(
        image,
        Rect(0, 0, image.width, image.height),
        Rect(0, 0, size.toInt(), size.toInt()), null
    )
    return result
}