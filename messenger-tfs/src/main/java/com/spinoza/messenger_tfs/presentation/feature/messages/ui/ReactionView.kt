package com.spinoza.messenger_tfs.presentation.feature.messages.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.presentation.util.getThemeColor

class ReactionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {

    var emoji = Emoji("", "")
        set(value) {
            field = value
            makeReaction()
        }
    var count = EMOJI_START_COUNT
        set(value) {
            field = value
            makeReaction()
        }
    var size = EMOJI_SIZE
        set(value) {
            field = value
            makeReaction()
        }

    var isCountVisible: Boolean = true
        set(value) {
            field = value
            makeReaction()
        }

    var isBackgroundVisible: Boolean = true

    private var reaction = ""
    private val cornerRadius = CORNER_RADIUS.dpToPx(this)
    private val selectedBackgroundColor = context.getThemeColor(R.attr.background_200_color)
    private val unselectedBackgroundColor = context.getThemeColor(R.attr.background_500_color)
    private val reactionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = context.getThemeColor(R.attr.text_300_color)
    }
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textBounds = Rect()

    init {
        context.withStyledAttributes(attrs, R.styleable.reaction_view) {
            size = this.getDimension(R.styleable.reaction_view_size, EMOJI_SIZE)
        }
    }

    fun setCustomPadding(padding: Float) {
        setCustomPadding(padding, padding, padding, padding)
    }

    fun setCustomPadding(left: Float, top: Float, right: Float, bottom: Float) {
        setPadding(
            left.dpToPx(this).toInt(),
            top.dpToPx(this).toInt(),
            right.dpToPx(this).toInt(),
            bottom.dpToPx(this).toInt(),
        )
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

        if (isBackgroundVisible) canvas.drawRoundRect(
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

    private fun makeReaction() {
        reaction =
            if (isCountVisible) "${emoji.toCharacterImage()} $count" else emoji.toCharacterImage()
        reactionPaint.textSize = size.spToPx(this)
    }

    private companion object {
        const val EMOJI_SIZE = 14f
        const val EMOJI_START_COUNT = 1
        const val CORNER_RADIUS = 10f
    }
}