package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.content.withStyledAttributes
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.dpToPx
import com.spinoza.messenger_tfs.getThemeColor
import com.spinoza.messenger_tfs.spToPx

class ReactionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var reaction = ""
    private var cornerRadius = getCornerRadius()
    private val symbolAdd = context.getString(R.string.symbol_add)

    var emoji = ""
        set(value) {
            field = value
            makeReaction()
        }
    var count = 0
        set(value) {
            field = value
            makeReaction()
        }
    var size = 0f
        set(value) {
            field = value
            makeReaction()
        }
    var isAddSymbol = false
        set(value) {
            field = value
            makeReaction()
        }

    private val selectedBackgroundColor =
        getThemeColor(context, R.attr.reaction_selected_background_color)
    private val unselectedBackgroundColor =
        getThemeColor(context, R.attr.reaction_unselected_background_color)
    private val textColorEmoji = getThemeColor(context, R.attr.reaction_text_color)
    private val textColorAdd = getThemeColor(context, R.attr.reaction_add_color)

    private val reactionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textBounds = Rect()

    init {
        context.withStyledAttributes(attrs, R.styleable.EmojiView) {
            emoji = this.getString(R.styleable.EmojiView_emoji) ?: ""
            count = this.getInt(R.styleable.EmojiView_count, 0)
            size = this.getFloat(R.styleable.EmojiView_size, EMOJI_SIZE)
        }
        val newPaddingLeft =
            maxOf(paddingLeft, DEFAULT_HORIZONTAL_PADDING.dpToPx(this).toInt())
        val newPaddingRight =
            maxOf(paddingLeft, DEFAULT_HORIZONTAL_PADDING.dpToPx(this).toInt())
        val newPaddingTop =
            maxOf(paddingLeft, DEFAULT_VERTICAL_PADDING.dpToPx(this).toInt())
        val newPaddingBottom =
            maxOf(paddingLeft, DEFAULT_VERTICAL_PADDING.dpToPx(this).toInt())
        setPadding(newPaddingLeft, newPaddingTop, newPaddingRight, newPaddingBottom)
        isClickable = true
    }

    fun copyBoundsFrom(source: View) {
        if (source.layoutParams is MarginLayoutParams) {
            layoutParams = source.layoutParams
            if (source is ReactionView) {
                size = source.size
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        reactionPaint.getTextBounds(reaction, 0, reaction.length, textBounds)
        val textWidth = textBounds.width()
        val textHeight = textBounds.height()
        val width = resolveSize(textWidth + paddingLeft + paddingRight, widthMeasureSpec)
        val height = resolveSize(textHeight + paddingTop + paddingBottom, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isAddSymbol) {
            reactionPaint.color = textColorAdd
            backgroundPaint.color = unselectedBackgroundColor
        } else {
            reactionPaint.color = textColorEmoji
            backgroundPaint.color =
                if (isSelected) selectedBackgroundColor else unselectedBackgroundColor
        }

        canvas.drawRoundRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            cornerRadius,
            cornerRadius,
            backgroundPaint
        )

        val offsetX = width.toFloat() / 2
        val offsetY = height / 2 - textBounds.exactCenterY()
        canvas.drawText(reaction, offsetX, offsetY, reactionPaint)
    }

    override fun performClick(): Boolean {
        isSelected = !isSelected
        return super.performClick()
    }

    override fun onSaveInstanceState(): Parcelable {
        val state = SavedState(super.onSaveInstanceState())
        state.isAddSymbol = isAddSymbol
        state.isSelected = isSelected
        state.count = count
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state)
            isAddSymbol = state.isAddSymbol
            isSelected = state.isSelected
            count = state.count

        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private fun makeReaction() {
        reaction = if (isAddSymbol) symbolAdd else "$emoji $count"
        reactionPaint.textSize = size.spToPx(this)
        cornerRadius = getCornerRadius()
        requestLayout()
    }

    private fun getCornerRadius(): Float = CORNER_RADIUS.dpToPx(this)

    private class SavedState : BaseSavedState, Parcelable {

        var isAddSymbol = false
        var isSelected = false
        var count = 0

        constructor(superState: Parcelable?) : super(superState)

        private constructor(source: Parcel) : super(source) {
            isAddSymbol = source.readInt() == 1
            isSelected = source.readInt() == 1
            count = source.readInt()
        }

        override fun writeToParcel(destination: Parcel, flags: Int) {
            super.writeToParcel(destination, flags)
            destination.writeInt(if (isAddSymbol) 1 else 0)
            destination.writeInt(if (isSelected) 1 else 0)
            destination.writeInt(count)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel) = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }

    private companion object {
        const val EMOJI_SIZE = 14f
        const val CORNER_RADIUS = 10f
        const val EMOJI_SCALE = 2
        const val DEFAULT_HORIZONTAL_PADDING = EMOJI_SIZE
        const val DEFAULT_VERTICAL_PADDING = EMOJI_SIZE / EMOJI_SCALE
    }
}