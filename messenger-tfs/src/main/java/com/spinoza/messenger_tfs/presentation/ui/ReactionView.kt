package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
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
    private val cornerRadius = CORNER_RADIUS.dpToPx(this)

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

    private val selectedBackgroundColor =
        getThemeColor(context, R.attr.reaction_selected_background_color)
    private val unselectedBackgroundColor =
        getThemeColor(context, R.attr.reaction_unselected_background_color)

    private val reactionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = getThemeColor(context, R.attr.reaction_text_color)
    }
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textBounds = Rect()

    init {
        context.withStyledAttributes(attrs, R.styleable.reaction_view) {
            emoji = this.getString(R.styleable.reaction_view_emoji) ?: ""
            count = this.getInt(R.styleable.reaction_view_count, 0)
            size = this.getDimension(R.styleable.reaction_view_size, EMOJI_SIZE)
        }
        val newPaddingLeft =
            maxOf(paddingLeft, DEFAULT_HORIZONTAL_PADDING.dpToPx(this).toInt())
        val newPaddingRight =
            maxOf(paddingRight, DEFAULT_HORIZONTAL_PADDING.dpToPx(this).toInt())
        val newPaddingTop =
            maxOf(paddingTop, DEFAULT_VERTICAL_PADDING.dpToPx(this).toInt())
        val newPaddingBottom =
            maxOf(paddingBottom, DEFAULT_VERTICAL_PADDING.dpToPx(this).toInt())
        setPadding(newPaddingLeft, newPaddingTop, newPaddingRight, newPaddingBottom)
        isClickable = true
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

        backgroundPaint.color =
            if (isSelected) selectedBackgroundColor else unselectedBackgroundColor

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
        state.isSelected = isSelected
        state.count = count
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state)
            isSelected = state.isSelected
            count = state.count

        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private fun makeReaction() {
        reaction = "$emoji $count"
        reactionPaint.textSize = size.spToPx(this)
    }

    private class SavedState : BaseSavedState, Parcelable {

        var isSelected = false
        var count = 0

        constructor(superState: Parcelable?) : super(superState)

        private constructor(source: Parcel) : super(source) {
            isSelected = source.readInt() == 1
            count = source.readInt()
        }

        override fun writeToParcel(destination: Parcel, flags: Int) {
            super.writeToParcel(destination, flags)
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
        const val DEFAULT_HORIZONTAL_PADDING = EMOJI_SIZE
        const val DEFAULT_VERTICAL_PADDING = EMOJI_SIZE / 2
    }
}