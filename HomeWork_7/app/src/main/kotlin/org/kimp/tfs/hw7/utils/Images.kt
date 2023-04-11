package org.kimp.tfs.hw7.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

fun Drawable.asBitmap() : Bitmap {
    if (this is BitmapDrawable) return this.bitmap
    return Bitmap.createBitmap(
        intrinsicWidth,
        intrinsicHeight,
        Bitmap.Config.ARGB_8888
    ).also { bm ->
        Canvas(bm).also { canvas ->
            this.setBounds(0, 0, canvas.width, canvas.height)
            this.draw(canvas)
        }
    }
}

fun Bitmap.circlize() : Bitmap {
    val circleSize = minOf(this.height, this.width)
    val r = circleSize / 2

    val result = Bitmap.createBitmap(
        circleSize, circleSize, Bitmap.Config.ARGB_8888
    )
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    Canvas(result).also { canvas ->
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(r.toFloat(), r.toFloat(), r.toFloat(), paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(
            this,
            Rect(
                this.width / 2 - r,
                this.height / 2 - r,
                this.width / 2 + r,
                this.height / 2 + r
            ),
            Rect(0, 0, circleSize, circleSize),
            paint
        )
    }

    return result
}
