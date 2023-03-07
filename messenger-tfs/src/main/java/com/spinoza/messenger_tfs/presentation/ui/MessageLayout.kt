package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.Cursor

class MessageLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    val name: TextView
    val message: TextView
    val reactions: FlexBoxLayout

    private var onAvatarClickListener: ((MessageLayout) -> Unit)? = null
    private var onMessageClickListener: ((MessageLayout) -> Unit)? = null
    private val avatar: ImageView
    private var cursor = Cursor()

    init {
        inflate(context, R.layout.message_layout, this)
        avatar = findViewById(R.id.avatar)
        name = findViewById(R.id.name)
        message = findViewById(R.id.message)
        reactions = findViewById(R.id.reactions)

        avatar.setOnClickListener { onAvatarClickListener?.invoke(this) }
        name.setOnClickListener { onMessageClickListener?.invoke(this) }
        message.setOnClickListener { onMessageClickListener?.invoke(this) }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        cursor.reset(paddingLeft, paddingTop)
        measureChildWithMargins(avatar, widthMeasureSpec, cursor.x, heightMeasureSpec, cursor.y)
        cursor.right(avatar.marginLeft + avatar.measuredWidth + avatar.marginRight)

        measureChildWithMargins(name, widthMeasureSpec, cursor.x, heightMeasureSpec, cursor.y)
        var textWidth = name.marginLeft + name.measuredWidth + name.marginRight
        cursor.down(name.marginTop + name.measuredHeight + name.marginBottom)

        measureChildWithMargins(message, widthMeasureSpec, cursor.x, heightMeasureSpec, cursor.y)
        textWidth =
            maxOf(textWidth, message.marginLeft + message.measuredWidth + message.marginRight)
        cursor.down(message.marginTop + message.measuredHeight + message.marginBottom)

        measureChildWithMargins(reactions, widthMeasureSpec, cursor.x, heightMeasureSpec, cursor.y)
        cursor.down(reactions.marginTop + reactions.measuredHeight + reactions.marginBottom)
        textWidth = maxOf(
            textWidth,
            reactions.marginLeft + reactions.measuredWidth + reactions.marginRight
        )

        val totalWidth = cursor.x + textWidth
        val totalHeight = cursor.y + paddingBottom
        setMeasuredDimension(totalWidth, totalHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        cursor.reset(paddingLeft, paddingTop)
        avatar.draw(cursor)
        cursor.right(avatar.marginLeft + avatar.measuredWidth + avatar.marginRight)

        name.draw(cursor)
        cursor.down(name.marginTop + name.measuredHeight + name.marginBottom)

        message.draw(cursor)
        cursor.down(message.marginTop + message.measuredHeight + message.marginBottom)

        reactions.draw(cursor)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams {
        return MarginLayoutParams(p)
    }

    override fun checkLayoutParams(p: LayoutParams): Boolean {
        return p is MarginLayoutParams
    }

    fun setAvatar(resId: Int) {
        avatar.setImageResource(resId)
    }

    fun setAvatar(bitmap: Bitmap) {
        avatar.setImageBitmap(bitmap)
    }

    fun setRoundAvatar(resId: Int) {
        val bitmap = BitmapFactory.decodeResource(resources, resId)
        setRoundAvatar(bitmap)
    }

    fun setRoundAvatar(bitmap: Bitmap) {
        val size = avatar.layoutParams.width.toFloat().dpToPx(this)
        setAvatar(bitmap.getRounded(size))
    }

    fun setOnAvatarClickListener(listener: (MessageLayout) -> Unit) {
        onAvatarClickListener = listener
    }

    fun setOnMessageClickListener(listener: (MessageLayout) -> Unit) {
        onMessageClickListener = listener
    }
}