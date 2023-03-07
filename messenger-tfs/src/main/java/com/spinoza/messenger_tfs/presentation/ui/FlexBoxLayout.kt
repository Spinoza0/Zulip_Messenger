package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import com.spinoza.messenger_tfs.R

class FlexBoxLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private var onIconAddClickListener: ((FlexBoxLayout) -> Unit)? = null
    private var internalMargin = 0
    private val row = RowHelper()
    private var offsetX = 0
    private var offsetY = 0

    private val iconAdd = ImageView(context, attrs, defStyleAttr, defStyleRes).apply {
        setImageResource(R.drawable.icon_add)
        setBackgroundResource(R.drawable.shape_flexboxlayout_icon_add)
        setOnClickListener { onIconAddClick() }

        val width = ICON_ADD_WIDTH.dpToPx(this@FlexBoxLayout).toInt()
        val height = ICON_ADD_HEIGHT.dpToPx(this@FlexBoxLayout).toInt()
        layoutParams = MarginLayoutParams(width, height)

        val iconPaddingLeft = ICON_ADD_HORIZONTAL_PADDING.dpToPx(this).toInt()
        val iconPaddingRight = ICON_ADD_HORIZONTAL_PADDING.dpToPx(this).toInt()
        val iconPaddingTop = ICON_ADD_VERTICAL_PADDING.dpToPx(this).toInt()
        val iconPaddingBottom = ICON_ADD_VERTICAL_PADDING.dpToPx(this).toInt()
        setPadding(iconPaddingLeft, iconPaddingTop, iconPaddingRight, iconPaddingBottom)
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.flexbox_layout) {
            internalMargin = getDimension(R.styleable.flexbox_layout_margin, 0f).toInt()
        }

        setIconAddVisibility(false)
        addView(iconAdd)
    }

    override fun addView(view: View) {
        super.addView(view, childCount - 1)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val layoutWidth = r - l - paddingLeft - paddingRight
        var viewWidth: Int
        var viewHeight: Int
        var offsetXWithMargin: Int
        var offsetYWithMargin: Int

        row.movePositionToStart()

        children.forEach { view ->
            if (view.isVisible) {
                viewWidth = view.getWidthWithMargins()
                viewHeight = view.getHeightWithMargins()
                if (row.needMovePositionToNextRow(viewWidth, layoutWidth)) {
                    row.movePositionToNextRow(viewHeight)
                }
                offsetXWithMargin = offsetX + view.marginLeft
                offsetYWithMargin = offsetY + view.marginTop
                view.layout(
                    offsetXWithMargin,
                    offsetYWithMargin,
                    offsetXWithMargin + view.measuredWidth,
                    offsetYWithMargin + view.measuredHeight
                )
                row.movePositionToRight(viewWidth)
                row.updateRowHeight(viewHeight)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val layoutWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        var viewWidth: Int
        var viewHeight: Int

        row.movePositionToStart()

        children.forEach { view ->
            if (view.isVisible) {
                measureView(view, widthMeasureSpec, heightMeasureSpec)
                viewWidth = view.getWidthWithMargins()
                viewHeight = view.getHeightWithMargins()
                if (row.needMovePositionToNextRow(viewWidth, layoutWidth)) {
                    row.movePositionToNextRow(viewHeight)
                }
                row.movePositionToRight(viewWidth)
                row.updateRowHeight(viewHeight)
            }
        }

        val width = resolveSize(row.maxWidth + paddingRight, widthMeasureSpec)
        val height =
            resolveSize(row.maxHeight + row.rowHeight + paddingBottom, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    fun setOnAddClickListener(listener: (FlexBoxLayout) -> Unit) {
        onIconAddClickListener = listener
    }

    fun setIconAddVisibility(state: Boolean) {
        iconAdd.isVisible = state
    }

    fun getIconAddVisibility() = iconAdd.isVisible

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

    private fun measureView(
        view: View,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        measureChildWithMargins(
            view,
            widthMeasureSpec,
            0,
            heightMeasureSpec,
            offsetY
        )
        val maxChildWidth = view.getWidthWithMargins()
        measureChildWithMargins(
            view,
            widthMeasureSpec,
            offsetX,
            heightMeasureSpec,
            offsetY
        )

        if (view.getWidthWithMargins() < maxChildWidth) {
            row.movePositionToNextRow(view.getHeightWithMargins())
            measureChildWithMargins(
                view,
                widthMeasureSpec,
                offsetX,
                heightMeasureSpec,
                offsetY
            )
        }
    }

    private fun onIconAddClick() {
        onIconAddClickListener?.invoke(this)
    }

    private inner class RowHelper {
        var maxHeight: Int = 0
            private set
        var maxWidth: Int = 0
            private set
        var rowHeight: Int = 0
            private set

        fun movePositionToStart() {
            offsetX = paddingLeft
            offsetY = paddingTop
            maxWidth = offsetX
            maxHeight = offsetY
            rowHeight = 0
        }

        fun needMovePositionToNextRow(viewWidth: Int, layoutWidth: Int) =
            offsetX + viewWidth + paddingRight + internalMargin > layoutWidth

        fun movePositionToNextRow(newRowHeight: Int) {
            offsetX = paddingLeft
            offsetY += rowHeight + internalMargin
            maxHeight = offsetY
            rowHeight = newRowHeight
        }

        fun updateRowHeight(newRowHeight: Int) {
            rowHeight = maxOf(rowHeight, newRowHeight)
        }

        fun movePositionToRight(offsetWidth: Int) {
            offsetX += offsetWidth + internalMargin
            maxWidth = maxOf(maxWidth, offsetX)
        }
    }

    private companion object {
        const val ICON_ADD_WIDTH = 45f
        const val ICON_ADD_HEIGHT = 30f
        const val ICON_ADD_HORIZONTAL_PADDING = 8f
        const val ICON_ADD_VERTICAL_PADDING = 4f
    }
}