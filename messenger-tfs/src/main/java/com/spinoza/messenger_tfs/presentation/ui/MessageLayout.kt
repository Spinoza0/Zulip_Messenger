package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import androidx.core.view.setMargins
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.dpToPx
import com.spinoza.messenger_tfs.spToPx

class MessageLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    var avatarImage = ImageView(context).apply {
        setImageResource(R.drawable.face)
    }
        set(value) {
            field = value
            setAvatarParams()
        }

    private val nameView = TextView(context)
    var name: String = ""
        set(value) {
            field = value
            nameView.text = value
            setTextParams(nameView, field, NAME_SIZE, R.attr.message_name_color)
        }

    private val messageView = TextView(context)
    var message: String = ""
        set(value) {
            field = value
            setTextParams(messageView, field, MESSAGE_SIZE, R.attr.message_text_color)
        }

    private val messagePaddingLeft = MESSAGE_PADDING_LEFT.dpToPx(this).toInt()
    private val messagePaddingRight = MESSAGE_PADDING_RIGHT.dpToPx(this).toInt()
    private val messagePaddingVertical = MESSAGE_PADDING_VERTICAL.dpToPx(this).toInt()
    private val messageMarginVertical = MESSAGE_MARGIN_VERTICAL.dpToPx(this).toInt()
    private val avatarMarginRight = AVATAR_MARGIN_RIGHT.dpToPx(this).toInt()
    private val nameMarginBottom = NAME_MARGIN_BOTTOM.dpToPx(this).toInt()
    private var offsetX = 0
    private var offsetY = 0

    val reactionsGroup = FlexBoxLayout(context).apply {
        val newLayoutParams =
            MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        newLayoutParams.setMargins(0)
        layoutParams = newLayoutParams
    }

    var onReactionAddClickListener: (() -> Unit)? = null
        set(value) {
            field = value
            reactionsGroup.onIconAddClickListener = value
        }

    init {
        setAvatarParams()
        setTextParams(nameView, "", NAME_SIZE, R.attr.message_name_color)
        setTextParams(messageView, "", MESSAGE_SIZE, R.attr.message_text_color)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        processLayout(
            widthMeasureSpec = widthMeasureSpec,
            heightMeasureSpec = heightMeasureSpec,
            measureFunc = ::measureView
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        processLayout(layoutFunc = ::viewLayout)
    }

    private fun processLayout(
        widthMeasureSpec: Int = 0,
        heightMeasureSpec: Int = 0,
        measureFunc: ((View, Int, Int) -> Unit)? = null,
        layoutFunc: ((View) -> Unit)? = null,
    ) {
        offsetX = marginLeft + paddingLeft
        offsetY = marginTop + paddingTop
        measureFunc?.let {
            measureFunc(avatarImage, widthMeasureSpec, heightMeasureSpec)
        }
        layoutFunc?.let { layoutFunc(avatarImage) }

        offsetX += avatarImage.measuredWidth + avatarMarginRight + messagePaddingLeft
        offsetY += messagePaddingVertical
        measureFunc?.let {
            measureFunc(nameView, widthMeasureSpec, heightMeasureSpec)
        }
        layoutFunc?.let { layoutFunc(nameView) }

        var textWidth = nameView.measuredWidth + messagePaddingLeft + messagePaddingRight
        offsetY += nameView.measuredHeight + nameMarginBottom
        measureFunc?.let {
            measureFunc(messageView, widthMeasureSpec, heightMeasureSpec)
        }
        layoutFunc?.let { layoutFunc(messageView) }

        textWidth = maxOf(
            textWidth,
            messageView.measuredWidth + messagePaddingLeft + messagePaddingRight
        )
        offsetX -= messagePaddingLeft
        offsetY += messageView.measuredHeight + messagePaddingVertical + messageMarginVertical
        measureFunc?.let {
            measureFunc(reactionsGroup, widthMeasureSpec, heightMeasureSpec)
        }
        layoutFunc?.let { layoutFunc(reactionsGroup) }

        measureFunc?.let {
            textWidth = maxOf(textWidth, reactionsGroup.measuredWidth)
            val totalWidth = offsetX + textWidth
            val totalHeight =
                offsetY + reactionsGroup.measuredHeight + marginBottom + paddingBottom
            setMeasuredDimension(totalWidth, totalHeight)
        }
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

    private fun viewLayout(view: View) {
        view.layout(
            offsetX,
            offsetY,
            offsetX + view.measuredWidth,
            offsetY + view.measuredHeight
        )
    }

    private fun measureView(view: View, widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildWithMargins(view, widthMeasureSpec, offsetX, heightMeasureSpec, offsetY)
    }

    private fun setTextParams(
        textView: TextView,
        text: String,
        size: Float,
        color: Int,
    ) {
        textView.text = text
        textView.textSize = size.spToPx(this)
        textView.setTextColor(color)
        textView.setBackgroundColor(R.attr.reaction_unselected_background_color)
        val layoutParams = MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(0)
        textView.layoutParams = layoutParams
    }

    private fun setAvatarParams() {
        val size = AVATAR_SIZE.dpToPx(this).toInt()
        val layoutParams = MarginLayoutParams(size, size)
        layoutParams.setMargins(0)
        avatarImage.layoutParams = layoutParams
        avatarImage.scaleType = ImageView.ScaleType.CENTER_CROP
    }

    private companion object {
        const val AVATAR_SIZE = 37f
        const val AVATAR_MARGIN_RIGHT = 9f
        const val NAME_SIZE = 14f
        const val NAME_MARGIN_BOTTOM = 4f
        const val MESSAGE_SIZE = 16f
        const val MESSAGE_PADDING_LEFT = 13f
        const val MESSAGE_PADDING_RIGHT = 13f
        const val MESSAGE_PADDING_VERTICAL = 8f
        const val MESSAGE_MARGIN_VERTICAL = 8f
    }
}