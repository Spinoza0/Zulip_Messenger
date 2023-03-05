package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.*
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.CursorXY
import com.spinoza.messenger_tfs.dpToPx

class FlexBoxLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private val cursor = FlexBoxLayoutCursor(CursorXY())

    var onIconAddClickListener: (() -> Unit)? = null
        set(value) {
            if (field == null) {
                if (value != null) {
                    addView(addIcon)
                }
                field = value
            } else if (value != null) {
                field = value
            } else {
                removeViewAt(childCount - 1)
            }
        }

    private val addIcon = ImageView(context, attrs, defStyleAttr, defStyleRes).apply {
        setImageResource(R.drawable.icon_add)
        setBackgroundResource(R.drawable.shape_flexboxlayout_icon_add)

        val width = ADD_ICON_WIDTH.dpToPx(this@FlexBoxLayout).toInt()
        val height = ADD_ICON_HEIGHT.dpToPx(this@FlexBoxLayout).toInt()
        val params = MarginLayoutParams(width, height)
        params.setMargins(0, 0, 0, 0)
        layoutParams = params

        val newPaddingLeft =
            maxOf(paddingLeft, ADD_ICON_HORIZONTAL_PADDING.dpToPx(this).toInt())
        val newPaddingRight =
            maxOf(paddingLeft, ADD_ICON_HORIZONTAL_PADDING.dpToPx(this).toInt())
        val newPaddingTop =
            maxOf(paddingTop, ADD_ICON_VERTICAL_PADDING.dpToPx(this).toInt())
        val newPaddingBottom =
            maxOf(paddingBottom, ADD_ICON_VERTICAL_PADDING.dpToPx(this).toInt())
        setPadding(newPaddingLeft, newPaddingTop, newPaddingRight, newPaddingBottom)
    }

    init {
        if (onIconAddClickListener != null) {
            addView(addIcon)
        }
    }

    override fun addView(child: View) {
        if (onIconAddClickListener != null)
            super.addView(child, childCount - 1)
        else {
            super.addView(child)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        processChildren(
            layoutWidth = r - l - marginLeft - marginRight - paddingLeft - paddingRight,
        ) { child, left, top, right, bottom ->
            child.layout(left, top, right, bottom)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        processChildren(
            layoutWidth = MeasureSpec.getSize(widthMeasureSpec) -
                    marginLeft - marginRight - paddingLeft - paddingRight,
            widthMeasureSpec = widthMeasureSpec,
            heightMeasureSpec = heightMeasureSpec,
            makeMeasure = true,
            setChildDimension = ::setMeasuredDimension
        )
    }

    private fun processChildren(
        layoutWidth: Int,
        widthMeasureSpec: Int = 0,
        heightMeasureSpec: Int = 0,
        makeMeasure: Boolean = false,
        setChildDimension: ((Int, Int) -> Unit)? = null,
        drawChild: ((View, Int, Int, Int, Int) -> Unit)? = null,
    ) {
        cursor.reset(marginLeft + paddingLeft, marginTop + paddingTop)
        children.forEach { child ->
            if (child.visibility != View.GONE) {
                if (makeMeasure) {
                    makeChildMeasure(child, widthMeasureSpec, heightMeasureSpec)
                }
                processChild(child, layoutWidth, drawChild)
            }
        }

        onIconAddClickListener?.let { clickListener ->
            addIcon.setOnClickListener { clickListener.invoke() }
        }

        setChildDimension?.let {
            cursor.addBounds()
            val width = resolveSize(cursor.maxWidth, widthMeasureSpec)
            val height = resolveSize(cursor.maxHeight, heightMeasureSpec)
            setChildDimension(width, height)
        }
    }

    private fun getChildHeight(child: View) =
        child.measuredHeight + child.marginTop + child.marginBottom

    private fun getChildWidth(child: View) =
        child.measuredWidth + child.marginLeft + child.marginRight

    private fun makeChildMeasure(
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

    private fun processChild(
        child: View,
        layoutWidth: Int,
        drawChild: ((View, Int, Int, Int, Int) -> Unit)?,
    ) {
        val childWidth = getChildWidth(child)
        val childHeight = getChildHeight(child)
        if (cursor.x + childWidth + marginRight + paddingRight > layoutWidth) {
            cursor.moveToNextLine(childHeight)
        } else {
            cursor.updateRowHeight(childHeight)
        }

        drawChild?.let { draw ->
            draw(child, cursor.x, cursor.y, cursor.x + childWidth, cursor.y + childHeight)
        }
        cursor.right(childWidth)
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

    private inner class FlexBoxLayoutCursor(private val cursorXY: CursorXY) {

        val x: Int
            get() = cursorXY.x
        val y: Int
            get() = cursorXY.y

        private var _maxHeight: Int = 0
        val maxHeight: Int
            get() = _maxHeight

        private var _maxWidth: Int = 0
        val maxWidth: Int
            get() = _maxWidth

        private var rowHeight: Int = 0
        private var startX: Int = 0
        private var startY: Int = 0

        init {
            reset(0, 0)
        }

        fun reset(x: Int, y: Int) {
            _maxHeight = 0
            _maxWidth = 0
            rowHeight = 0
            startX = x
            startY = y
            cursorXY.reset(startX, startY)
        }

        fun moveToNextLine(newRowHeight: Int) {
            cursorXY.resetX(startX)
            val deltaY = rowHeight + marginTop
            cursorXY.down(deltaY)
            _maxHeight += deltaY
            rowHeight = newRowHeight
        }

        fun updateRowHeight(newRowHeight: Int) {
            rowHeight = maxOf(rowHeight, newRowHeight)
        }

        fun right(offsetWidth: Int) {
            cursorXY.right(offsetWidth + marginRight)
            _maxWidth = maxOf(_maxWidth, x)
        }

        fun addBounds() {
            _maxHeight += marginTop + marginBottom + paddingTop + paddingBottom + rowHeight
            _maxWidth += marginLeft + marginRight + paddingLeft + paddingRight
        }
    }

    private companion object {
        const val ADD_ICON_WIDTH = 45f
        const val ADD_ICON_HEIGHT = 30f
        const val ADD_ICON_HORIZONTAL_PADDING = 8f
        const val ADD_ICON_VERTICAL_PADDING = 4f
    }
}