package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.withStyledAttributes
import androidx.core.view.*
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.CursorXY
import com.spinoza.messenger_tfs.dpToPx
import com.spinoza.messenger_tfs.drawView

class FlexBoxLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private val cursor = FlexBoxLayoutCursor(CursorXY())
    private var internalMargin = 0

    private val addIcon = ImageView(context, attrs, defStyleAttr, defStyleRes).apply {
        setImageResource(R.drawable.icon_add)
        setBackgroundResource(R.drawable.shape_flexboxlayout_icon_add)

        val width = ADD_ICON_WIDTH.dpToPx(this@FlexBoxLayout).toInt()
        val height = ADD_ICON_HEIGHT.dpToPx(this@FlexBoxLayout).toInt()
        layoutParams = MarginLayoutParams(width, height)

        val iconPaddingLeft = ADD_ICON_HORIZONTAL_PADDING.dpToPx(this).toInt()
        val iconPaddingRight = ADD_ICON_HORIZONTAL_PADDING.dpToPx(this).toInt()
        val iconPaddingTop = ADD_ICON_VERTICAL_PADDING.dpToPx(this).toInt()
        val iconPaddingBottom = ADD_ICON_VERTICAL_PADDING.dpToPx(this).toInt()
        setPadding(iconPaddingLeft, iconPaddingTop, iconPaddingRight, iconPaddingBottom)

        addView(this)
    }

    var onIconAddClickListener: (() -> Unit)? = null
        set(value) {
            if (value != null) {
                field = value
                addIcon.visibility = VISIBLE
                addIcon.setOnClickListener { value.invoke() }
            } else {
                addIcon.visibility = GONE
            }
        }

    init {
        context.withStyledAttributes(attrs, R.styleable.flexbox_layout) {
            internalMargin = getDimension(R.styleable.flexbox_layout_margin, 0f).toInt()
        }

        addIcon.visibility = if (onIconAddClickListener != null) VISIBLE else GONE
    }

    override fun addView(view: View) {
        super.addView(view, childCount - 1)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        processLayout(
            layoutWidth = r - l - paddingLeft - paddingRight,
            layoutFunc = ::drawView
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        processLayout(
            layoutWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight,
            widthMeasureSpec = widthMeasureSpec,
            heightMeasureSpec = heightMeasureSpec,
            measureFunc = ::measureView
        )
    }

    private fun processLayout(
        layoutWidth: Int,
        widthMeasureSpec: Int = 0,
        heightMeasureSpec: Int = 0,
        measureFunc: ((View, Int, Int) -> Unit)? = null,
        layoutFunc: ((View, CursorXY) -> Unit)? = null,
    ) {
        cursor.reset()
        var viewWidth = 0
        var viewHeight = 0

        children.forEach { view ->
            if (view.visibility != GONE) {
                measureFunc?.let {
                    measureFunc(view, widthMeasureSpec, heightMeasureSpec)
                    viewWidth = getChildWidth(view)
                    viewHeight = getChildHeight(view)
                    if (cursor.needMoveToNextLine(viewWidth, layoutWidth)) {
                        cursor.moveToNextLine(viewHeight)
                    }
                }

                layoutFunc?.let {
                    viewWidth = getChildWidth(view)
                    viewHeight = getChildHeight(view)
                    if (cursor.needMoveToNextLine(viewWidth, layoutWidth)) {
                        cursor.moveToNextLine(viewHeight)
                    }
                    layoutFunc(view, cursor.cursorXY)
                }

                cursor.right(viewWidth)
                cursor.updateRowHeight(viewHeight)
            }
        }

        measureFunc?.let {
            cursor.moveToEnd()
            val width = resolveSize(cursor.x, widthMeasureSpec)
            val height = resolveSize(cursor.y, heightMeasureSpec)
            setMeasuredDimension(width, height)
        }
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

    private inner class FlexBoxLayoutCursor(val cursorXY: CursorXY) {

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
            cursorXY.reset(0, 0)
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
            cursorXY.resetX(startX)
            val deltaY = rowHeight + internalMargin
            cursorXY.down(deltaY)
            maxHeight += deltaY
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
        const val ADD_ICON_WIDTH = 45f
        const val ADD_ICON_HEIGHT = 30f
        const val ADD_ICON_HORIZONTAL_PADDING = 8f
        const val ADD_ICON_VERTICAL_PADDING = 4f
    }
}