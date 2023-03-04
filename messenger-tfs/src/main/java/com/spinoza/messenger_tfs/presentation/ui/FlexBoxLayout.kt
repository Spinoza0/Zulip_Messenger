package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.*
import com.spinoza.messenger_tfs.domain.CursorXY

class FlexBoxLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private val offset = Offset()

    var onIconAddClickListener: (() -> Unit)? = null
        set(value) {
            if (field == null) {
                if (value != null) {
                    addView(symbolAdd)
                }
                field = value
            } else if (value != null) {
                field = value
            } else {
                removeViewAt(childCount - 1)
            }
        }

    private val symbolAdd =
        ReactionView(context, attrs, defStyleAttr, defStyleRes).apply {
            isAddSymbol = true
        }

    init {
        if (onIconAddClickListener != null) {
            addView(symbolAdd)
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
        offset.reset(marginLeft + paddingLeft, marginTop + paddingTop)
        children.forEachIndexed { index, child ->
            if (child.visibility != View.GONE) {
                if (makeMeasure) {
                    if (onIconAddClickListener != null &&
                        childCount > 1 &&
                        childCount - 1 == index
                    ) {
                        symbolAdd.copyBoundsFrom(getChildAt(index - 1))
                    }
                    makeChildMeasure(child, widthMeasureSpec, heightMeasureSpec)
                }
                processChild(child, layoutWidth, drawChild)
            }
        }

        onIconAddClickListener?.let { clickListener ->
            symbolAdd.setOnClickListener { clickListener.invoke() }
        }

        setChildDimension?.let {
            offset.addBounds()
            val width = resolveSize(offset.maxWidth, widthMeasureSpec)
            val height = resolveSize(offset.maxHeight, heightMeasureSpec)
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
            offset.y
        )
        val maxChildWidth = getChildWidth(child)
        measureChildWithMargins(
            child,
            widthMeasureSpec,
            offset.x,
            heightMeasureSpec,
            offset.y
        )

        if (getChildWidth(child) < maxChildWidth) {
            offset.moveToNextLine(getChildHeight(child))
            measureChildWithMargins(
                child,
                widthMeasureSpec,
                offset.x,
                heightMeasureSpec,
                offset.y
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
        if (offset.x + childWidth + marginRight + paddingRight > layoutWidth) {
            offset.moveToNextLine(childHeight)
        } else {
            offset.updateRowHeight(childHeight)
        }

        drawChild?.let { draw ->
            draw(child, offset.x, offset.y, offset.x + childWidth, offset.y + childHeight)
        }
        offset.right(childWidth)
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

    private inner class Offset {

        private val cursor = CursorXY()

        val x: Int
            get() = cursor.x
        val y: Int
            get() = cursor.y

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
            cursor.reset(startX, startY)
        }

        fun moveToNextLine(newRowHeight: Int) {
            cursor.resetX(startX)
            val deltaY = rowHeight + marginTop
            cursor.down(deltaY)
            _maxHeight += deltaY
            rowHeight = newRowHeight
        }

        fun updateRowHeight(newRowHeight: Int) {
            rowHeight = maxOf(rowHeight, newRowHeight)
        }

        fun right(offsetWidth: Int) {
            cursor.right(offsetWidth + marginRight)
            _maxWidth = maxOf(_maxWidth, x)
        }

        fun addBounds() {
            _maxHeight += marginTop + marginBottom + paddingTop + paddingBottom + rowHeight
            _maxWidth += marginLeft + marginRight + paddingLeft + paddingRight
        }
    }
}