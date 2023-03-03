package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.*

class FlexBoxLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ViewGroup(context, attrs, defStyleAttr) {

    // TODO: work with onAddButtonListener
    var onAddButtonListener: (() -> Unit)? = null

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        processChildren(
            layoutWidth = r - l - marginLeft - marginRight - paddingLeft - paddingRight,
            childLayout = { child, left, top, right, bottom ->
                child.layout(left, top, right, bottom)
            }
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        processChildren(
            layoutWidth = MeasureSpec.getSize(widthMeasureSpec) -
                    marginLeft - marginRight - paddingLeft - paddingRight,
            widthMeasureSpec = widthMeasureSpec,
            heightMeasureSpec = heightMeasureSpec,
            prepareChildSize = ::measureChildWithMargins,
            setChildDimension = ::setMeasuredDimension
        )
    }

    private fun processChildren(
        layoutWidth: Int,
        widthMeasureSpec: Int = 0,
        heightMeasureSpec: Int = 0,
        prepareChildSize: ((View, Int, Int, Int, Int) -> Unit)? = null,
        setChildDimension: ((Int, Int) -> Unit)? = null,
        childLayout: ((View, Int, Int, Int, Int) -> Unit)? = null,
    ) {
        val offset = Offset()
        children.forEach { child ->
            if (child.visibility != View.GONE) {

                prepareChildSize?.let {
                    prepareChildSize(
                        child,
                        widthMeasureSpec,
                        0,
                        heightMeasureSpec,
                        offset.y
                    )
                    val maxChildWidth = getChildWidth(child)
                    prepareChildSize(
                        child,
                        widthMeasureSpec,
                        offset.x,
                        heightMeasureSpec,
                        offset.y
                    )

                    if (getChildWidth(child) < maxChildWidth) {
                        offset.moveToNextLine(getChildHeight(child))
                        prepareChildSize(
                            child,
                            widthMeasureSpec,
                            offset.x,
                            heightMeasureSpec,
                            offset.y
                        )
                    }
                }

                val childWidth = getChildWidth(child)
                val childHeight = getChildHeight(child)
                if (offset.x + childWidth + offset.rightDeltaX > layoutWidth) {
                    offset.moveToNextLine(childHeight)
                } else {
                    offset.updateRowHeight(childHeight)
                }

                childLayout?.let { draw ->
                    draw(child, offset.x, offset.y, offset.x + childWidth, offset.y + childHeight)
                }
                offset.increaseX(childWidth)
            }
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

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams {
        return MarginLayoutParams(p)
    }

    private inner class Offset {

        private var _maxHeight: Int = 0
        val maxHeight: Int
            get() = _maxHeight

        private var _maxWidth: Int = 0
        val maxWidth: Int
            get() = _maxWidth

        private var rowHeight: Int = 0
        private val leftDeltaX: Int = marginLeft + paddingLeft
        private val topDeltaY: Int = marginTop + paddingTop
        val rightDeltaX: Int = marginRight + paddingRight
        var x: Int = leftDeltaX
        var y: Int = topDeltaY

        fun moveToNextLine(newRowHeight: Int) {
            x = leftDeltaX
            val deltaY = rowHeight + marginTop
            y += deltaY
            _maxHeight += deltaY
            rowHeight = newRowHeight
        }

        fun updateRowHeight(newRowHeight: Int) {
            rowHeight = maxOf(rowHeight, newRowHeight)
        }

        fun increaseX(offsetWidth: Int) {
            x += offsetWidth + marginRight
            _maxWidth = maxOf(_maxWidth, x)
        }

        fun addBounds() {
            _maxHeight += marginTop + marginBottom + paddingTop + paddingBottom + rowHeight
            _maxWidth += marginLeft + marginRight + paddingLeft + paddingRight
        }
    }
}