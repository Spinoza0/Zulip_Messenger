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
import com.bumptech.glide.load.model.LazyHeaders
import com.spinoza.messenger_tfs.BuildConfig
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
        imageUrl: String,
        textView: TextView,
        authData: String,
    ) {
        val fullUrl = if (isFullUrl(imageUrl)) imageUrl else "${IMAGE_BASE_URL}$imageUrl"
        runCatching {
            val glideUrl = GlideUrl(
                fullUrl,
                LazyHeaders.Builder().addHeader(HEADER_AUTHORIZATION, authData).build()
            )
            val bitmap = Glide.with(context)
                .asBitmap()
                .load(glideUrl)
                .submit().get()
            val newDrawable = BitmapDrawable(resources, bitmap)
            val scale =
                textView.width / newDrawable.intrinsicWidth.toFloat() / IMAGE_SCALE
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

    private fun isFullUrl(url: String): Boolean {
        return url.startsWith(IMAGE_URL_SECURED_PREFIX, ignoreCase = true) ||
                url.startsWith(IMAGE_URL_BASIC_PREFIX, ignoreCase = true)
    }

    private companion object {

        const val IMAGE_TOP_BOUND = 0
        const val IMAGE_LEFT_BOUND = 0
        const val IMAGE_SCALE = 1.25f
        const val IMAGE_BASE_URL = BuildConfig.ZULIP_SERVER_URL
        const val IMAGE_URL_SECURED_PREFIX = "https://"
        const val IMAGE_URL_BASIC_PREFIX = "http://"
        const val HEADER_AUTHORIZATION = "Authorization"
    }
}