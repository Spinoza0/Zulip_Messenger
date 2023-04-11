package org.kimp.tfs.hw7.utils

import android.graphics.Paint
import android.graphics.Rect

fun Paint.getTextBounds(text: String): Rect =
    Rect().also { this.getTextBounds(text, 0, text.length, it) }
