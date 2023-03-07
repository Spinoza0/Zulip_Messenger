package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.withStyledAttributes
import androidx.core.view.*
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.Cursor

class FlexBoxLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private var onIconAddClickListener: ((FlexBoxLayout) -> Unit)? = null
    private val cursor = FlexBoxLayoutCursor(Cursor())
    private var internalMargin = 0
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
        cursor.reset()

        var viewWidth: Int
        var viewHeight: Int

        children.forEach { view ->
            if (view.visibility != GONE) {
                viewWidth = getChildWidth(view)
                viewHeight = getChildHeight(view)
                if (cursor.needMoveToNextLine(viewWidth, layoutWidth)) {
                    cursor.moveToNextLine(viewHeight)
                }
                view.draw(cursor.cursorXY)
                cursor.right(viewWidth)
                cursor.updateRowHeight(viewHeight)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val layoutWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        var viewWidth: Int
        var viewHeight: Int

        cursor.reset()

        children.forEach { view ->
            if (view.visibility != GONE) {
                measureView(view, widthMeasureSpec, heightMeasureSpec)
                viewWidth = getChildWidth(view)
                viewHeight = getChildHeight(view)
                if (cursor.needMoveToNextLine(viewWidth, layoutWidth)) {
                    cursor.moveToNextLine(viewHeight)
                }

                cursor.right(viewWidth)
                cursor.updateRowHeight(viewHeight)
            }
        }


        cursor.moveToEnd()
        val width = resolveSize(cursor.x, widthMeasureSpec)
        val height = resolveSize(cursor.y, heightMeasureSpec)
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

    private fun getChildHeight(child: View) =
        child.measuredHeight + child.marginTop + child.marginBottom

    private fun getChildWidth(child: View) =
        child.measuredWidth + child.marginLeft + child.marginRight

    private fun measureView(
        child: View,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        measureChildWithMargins(
            child,
            widthMeasureSpec,
            0,
            heightMeasureSpec,
            cursor.y
        )
        val maxChildWidth = getChildWidth(child)
        measureChildWithMargins(
            child,
            widthMeasureSpec,
            cursor.x,
            heightMeasureSpec,
            cursor.y
        )

        if (getChildWidth(child) < maxChildWidth) {
            cursor.moveToNextLine(getChildHeight(child))
            measureChildWithMargins(
                child,
                widthMeasureSpec,
                cursor.x,
                heightMeasureSpec,
                cursor.y
            )
        }
    }

    private fun onIconAddClick() {
        onIconAddClickListener?.invoke(this)
    }

    private inner class FlexBoxLayoutCursor(val cursorXY: Cursor) {

        val x: Int
            get() = cursorXY.x
        val y: Int
            get() = cursorXY.y

        private var maxHeight: Int = 0
        private var maxWidth: Int = 0
        private var rowHeight: Int = 0
        private var startX: Int = 0
        private var startY: Int = 0

        init {
            cursorXY.reset()
        }

        fun reset() {
            rowHeight = 0
            maxHeight = paddingTop
            maxWidth = paddingLeft
            startY = paddingTop
            startX = paddingLeft
            cursorXY.reset(startX, startY)
        }

        fun needMoveToNextLine(viewWidth: Int, layoutWidth: Int) =
            cursor.x + viewWidth + paddingRight + internalMargin > layoutWidth

        fun moveToNextLine(newRowHeight: Int) {
            val offsetY = rowHeight + internalMargin
            cursorXY.reset(x = startX)
            cursorXY.down(offsetY)
            maxHeight += offsetY
            rowHeight = newRowHeight
        }

        fun updateRowHeight(newRowHeight: Int) {
            rowHeight = maxOf(rowHeight, newRowHeight)
        }

        fun right(offsetWidth: Int) {
            cursorXY.right(offsetWidth + internalMargin)
            maxWidth = maxOf(maxWidth, x)
        }

        fun moveToEnd() {
            cursorXY.reset()
            cursorXY.down(maxHeight + rowHeight + paddingBottom)
            cursorXY.right(maxWidth + paddingRight)
        }
    }

    private companion object {
        const val ICON_ADD_WIDTH = 45f
        const val ICON_ADD_HEIGHT = 30f
        const val ICON_ADD_HORIZONTAL_PADDING = 8f
        const val ICON_ADD_VERTICAL_PADDING = 4f
    }
}