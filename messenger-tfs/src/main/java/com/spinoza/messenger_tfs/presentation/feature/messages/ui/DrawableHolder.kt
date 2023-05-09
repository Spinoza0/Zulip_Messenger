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
import com.spinoza.messenger_tfs.presentation.util.getAppComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DrawableHolder(private val resources: Resources, bitmap: Bitmap? = null) :
    BitmapDrawable(resources, bitmap) {

    private var drawable: Drawable? = null

    override fun draw(canvas: Canvas) {
        drawable?.run { draw(canvas) }
    }

    suspend fun loadImage(context: Context, imageUrl: String, textView: TextView) {
        runCatching {
            val appComponent = context.getAppComponent()
            val webUtil = appComponent.getWebUtil()
            val fullUrl = webUtil.getFullUrl(imageUrl)
            val headers = LazyHeaders.Builder().addHeader(
                appComponent.getAuthorizationStorage().getAuthHeaderTitle(),
                appComponent.getAuthorizationStorage().getAuthHeaderValue()
            ).build()
            val glideUrl = GlideUrl(fullUrl, headers)
            val bitmap = Glide.with(context)
                .asBitmap()
                .load(glideUrl)
                .submit().get()
            val newDrawable = BitmapDrawable(resources, bitmap)
            val width: Int
            val height: Int
            if (textView.maxWidth < Int.MAX_VALUE) {
                val scale = newDrawable.bitmap.width.toFloat() / textView.maxWidth
                width = textView.maxWidth - textView.paddingStart - textView.paddingEnd
                height = (newDrawable.bitmap.height / scale).toInt()
            } else {
                val scaleUrlType =
                    if (webUtil.isUserUploadsUrl(fullUrl)) USER_IMAGE_SCALE else ZULIP_IMAGE_SCALE
                val scale = textView.width / newDrawable.intrinsicWidth.toFloat() / scaleUrlType
                width = (newDrawable.intrinsicWidth * scale).toInt()
                height = (newDrawable.intrinsicHeight * scale).toInt()
            }
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