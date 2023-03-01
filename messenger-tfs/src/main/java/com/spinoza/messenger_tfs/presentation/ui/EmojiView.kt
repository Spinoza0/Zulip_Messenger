package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

class EmojiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    var emoji = ""
        set(value) {
            field = value
            updateEmoji()
        }
    var count = 0
        set(value) {
            field = value
            updateEmoji()
        }
    var size = EMOJI_SIZE
        set(value) {
            field = value
            updateEmoji()
        }
    private var fullEmoji: String = ""
    private var emojiWidth = 0f

    // TODO: save state (destroy activity)
    private var emojiIsSelected: Boolean = false

    private var cornerRadius = getCornerRadius()
    private val emojiPadding = EMOJI_PADDING.spToPx()

    private val selectedBackgroundColor = Color.parseColor("#3A3A3A")
    private val unselectedBackgroundColor = Color.parseColor("#1C1C1C")
    private val textColor = Color.parseColor("#CCCCCC")

    private val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size.spToPx()
        color = textColor
        textAlign = Paint.Align.CENTER
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundRect = RectF()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val emojiHeight = (size * EMOJI_SCALE).spToPx()
        val width = resolveSize(emojiWidth.toInt(), widthMeasureSpec)
        val height = resolveSize(emojiHeight.toInt(), heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        backgroundPaint.color =
            if (emojiIsSelected) selectedBackgroundColor else unselectedBackgroundColor

        backgroundRect.apply {
            left = 0f
            top = 0f
            right = width.toFloat()
            bottom = height.toFloat()
        }
        canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, backgroundPaint)
        val x = width.toFloat() / EMOJI_SCALE
        val y = (height - (emojiPaint.descent() + emojiPaint.ascent())) / EMOJI_SCALE
        canvas.drawText(fullEmoji, x, y, emojiPaint)
    }


    override fun setSelected(selected: Boolean) {
        emojiIsSelected = selected
        invalidate()
    }

    override fun isSelected(): Boolean {
        return emojiIsSelected
    }

    private fun Float.spToPx(): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this,
            resources.displayMetrics
        )
    }

    // TODO: add theme support
    private fun getThemeColor(attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    private fun updateEmoji() {
        fullEmoji = "$emoji $count"
        emojiPaint.textSize = size.spToPx()
        cornerRadius = getCornerRadius()
        emojiWidth = emojiPadding + emojiPaint.measureText(fullEmoji) + emojiPadding
        requestLayout()
    }

    private fun getCornerRadius(): Float = (size * EMOJI_SCALE / CORNER_RADIUS_SCALE).spToPx()

    companion object {
        private const val EMOJI_SIZE = 14f
        private const val CORNER_RADIUS_SCALE = 3f
        private const val EMOJI_SCALE = 2f
        private const val EMOJI_PADDING = EMOJI_SIZE
    }
}