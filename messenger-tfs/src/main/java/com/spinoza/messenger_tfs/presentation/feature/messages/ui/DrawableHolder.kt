package com.spinoza.messenger_tfs.presentation.feature.messages.ui

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.spinoza.messenger_tfs.domain.webutil.WebUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DrawableHolder(private val resources: Resources, bitmap: Bitmap? = null) :
    BitmapDrawable(resources, bitmap) {

    private var drawable: Drawable? = null

    override fun draw(canvas: Canvas) {
        drawable?.run { draw(canvas) }
    }

    suspend fun loadImage(
        context: Context,
        webUtil: WebUtil,
        imageUrl: String,
        textView: TextView,
    ) {
        runCatching {
            val fullUrl = webUtil.getFullUrl(imageUrl)
            val glideUrl = GlideUrl(fullUrl, webUtil.getLazyHeaders())
            val bitmap = Glide.with(context)
                .asBitmap()
                .load(glideUrl)
                .submit().get()
            val newDrawable = BitmapDrawable(resources, bitmap)
            val scale = newDrawable.bitmap.width.toFloat() / textView.maxWidth
            val width: Int = textView.maxWidth - textView.paddingStart - textView.paddingEnd
            val height = (newDrawable.bitmap.height / scale).toInt()
            newDrawable.setBounds(IMAGE_LEFT_BOUND, IMAGE_TOP_BOUND, width, height)
            drawable = newDrawable
            setBounds(IMAGE_LEFT_BOUND, IMAGE_TOP_BOUND, width, height)
            withContext(Dispatchers.Main) {
                textView.text = textView.text
            }
        }
    }

    private companion object {

        const val IMAGE_TOP_BOUND = 0
        const val IMAGE_LEFT_BOUND = 0
    }
}