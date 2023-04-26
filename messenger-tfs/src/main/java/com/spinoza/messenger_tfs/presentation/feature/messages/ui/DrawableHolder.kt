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
            val scaleUsingUrlType =
                if (webUtil.isUserUploadsUrl(fullUrl)) USER_IMAGE_SCALE else ZULIP_IMAGE_SCALE
            val scale = textView.width / newDrawable.intrinsicWidth.toFloat() / scaleUsingUrlType
            val width = (newDrawable.intrinsicWidth * scale).toInt()
            val height = (newDrawable.intrinsicHeight * scale).toInt()
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
        const val ZULIP_IMAGE_SCALE = 1.25f
        const val USER_IMAGE_SCALE = 0.5f
    }
}