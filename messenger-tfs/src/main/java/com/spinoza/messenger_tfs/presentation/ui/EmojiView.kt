package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
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
            if (isSelected) selectedBackgroundColor else unselectedBackgroundColor

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

    override fun performClick(): Boolean {
        isSelected = !isSelected
        return super.performClick()
    }

    override fun onSaveInstanceState(): Parcelable {
        val state = SavedState(super.onSaveInstanceState())
        state.selected = isSelected
        state.count = count
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state)
            isSelected = state.selected
            count = state.count

        } else {
            super.onRestoreInstanceState(state)
        }
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

    private class SavedState : BaseSavedState, Parcelable {

        var selected = false
        var count = 0

        constructor(superState: Parcelable?) : super(superState)

        private constructor(source: Parcel) : super(source) {
            selected = source.readInt() == 1
            count = source.readInt()
        }

        override fun writeToParcel(destination: Parcel, flags: Int) {
            super.writeToParcel(destination, flags)
            destination.writeInt(if (selected) 1 else 0)
            destination.writeInt(count)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel) = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }

    companion object {
        private const val EMOJI_SIZE = 14f
        private const val CORNER_RADIUS_SCALE = 3f
        private const val EMOJI_SCALE = 2f
        private const val EMOJI_PADDING = EMOJI_SIZE
    }
}