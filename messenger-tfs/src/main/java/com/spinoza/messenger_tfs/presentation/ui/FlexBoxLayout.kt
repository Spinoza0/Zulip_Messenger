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
            parentWidthMeasureSpec = 0,
            parentHeightMeasureSpec = 0,
            childLayout = { child, left, top, right, bottom ->
                child.layout(left, top, right, bottom)
            }
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        processChildren(
            layoutWidth = MeasureSpec.getSize(widthMeasureSpec) -
                    marginLeft - marginRight - paddingLeft - paddingRight,
            parentWidthMeasureSpec = widthMeasureSpec,
            parentHeightMeasureSpec = heightMeasureSpec,
            prepareChildSize = ::measureChildWithMargins,
            setChildDimension = ::setMeasuredDimension
        )
    }

    private fun processChildren(
        layoutWidth: Int,
        parentWidthMeasureSpec: Int,
        parentHeightMeasureSpec: Int,
        prepareChildSize: ((View, Int, Int, Int, Int) -> Unit)? = null,
        setChildDimension: ((Int, Int) -> Unit)? = null,
        childLayout: ((View, Int, Int, Int, Int) -> Unit)? = null,
    ) {
        // val offset = Offset()
        var rowHeight: Int = 0
        var maxHeight: Int = 0
        var maxWidth: Int = 0
        val leftDeltaX: Int = marginLeft + paddingLeft
        val topDeltaY: Int = marginTop + paddingTop
        val rightDeltaX: Int = marginRight + paddingRight
        var offsetX: Int = leftDeltaX
        var offsetY: Int = topDeltaY

        children.forEach { child ->
            if (child.visibility != View.GONE) {

                prepareChildSize?.let {
                    prepareChildSize(
                        child,
                        parentWidthMeasureSpec,
                        0,
                        parentHeightMeasureSpec,
                        offsetY
                        //offset.y
                    )
                    val maxChildWidth = getChildWidth(child)
                    prepareChildSize(
                        child,
                        parentWidthMeasureSpec,
                        offsetX,
                        // offset.x,
                        parentHeightMeasureSpec,
                        offsetY
                        //offset.y
                    )

                    if (getChildWidth(child) < maxChildWidth) {
                        offsetX = leftDeltaX
                        val deltaY = rowHeight + marginTop
                        offsetY += deltaY
                        maxHeight += deltaY
                        rowHeight = getChildHeight(child)
                        //offset.moveToNextLine(getChildHeight(child))
                        prepareChildSize(
                            child,
                            parentWidthMeasureSpec,
                            offsetX,
                            //offset.x,
                            parentHeightMeasureSpec,
                            offsetY
                            //offset.y
                        )
                    }
                }

                val childWidth = getChildWidth(child)
                val childHeight = getChildHeight(child)
                if (offsetX + childWidth + rightDeltaX > layoutWidth) {
                    //if (offset.x + childWidth + offset.rightDeltaX > layoutWidth) {
                    offsetX = leftDeltaX
                    val deltaY = rowHeight + marginTop
                    offsetY += deltaY
                    maxHeight += deltaY
                    rowHeight = childHeight
//                    offset.moveToNextLine(childHeight)
                } else {
                    rowHeight = maxOf(rowHeight, childHeight)
//                    offset.updateRowHeight(childHeight)
                }

                childLayout?.let { draw ->
                    draw(child, offsetX, offsetY, offsetX + childWidth, offsetY + childHeight)
//                    draw(child, offset.x, offset.y, offset.x + childWidth, offset.y + childHeight)
                }
                offsetX += childWidth + marginRight
                maxWidth = maxOf(maxWidth, offsetX)
//                offset.increaseX(childWidth)
            }
        }

        setChildDimension?.let {
            maxHeight += marginTop + marginBottom + paddingTop + paddingBottom + rowHeight
            maxWidth += marginLeft + marginRight + paddingLeft + paddingRight
            val width = resolveSize(maxWidth, parentWidthMeasureSpec)
            val height = resolveSize(maxHeight, parentHeightMeasureSpec)
//            offset.addBounds()
//            val width = resolveSize(offset.maxWidth, parentWidthMeasureSpec)
//            val height = resolveSize(offset.maxHeight, parentHeightMeasureSpec)
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

//    private inner class Offset(
//        private var rowHeight: Int = 0,
//        private var _maxHeight: Int = 0,
//        private var _maxWidth: Int = 0,
//        private val leftDeltaX: Int = marginLeft + paddingLeft,
//        private val topDeltaY: Int = marginTop + paddingTop,
//        val rightDeltaX: Int = marginRight + paddingRight,
//        var x: Int = leftDeltaX,
//        var y: Int = topDeltaY,
//        val maxHeight: Int = _maxHeight,
//        val maxWidth: Int = _maxWidth,
//    ) {
//
//        fun moveToNextLine(newRowHeight: Int) {
//            x = leftDeltaX
//            val deltaY = rowHeight + marginTop
//            y += deltaY
//            _maxHeight += deltaY
//            rowHeight = newRowHeight
//        }
//
//        fun updateRowHeight(newRowHeight: Int) {
//            rowHeight = maxOf(rowHeight, newRowHeight)
//        }
//
//        fun increaseX(offsetWidth: Int) {
//            x += offsetWidth + marginRight
//            _maxWidth = maxOf(_maxWidth, x)
//        }
//
//        fun addBounds() {
//            _maxHeight += marginTop + marginBottom + paddingTop + paddingBottom + rowHeight
//            _maxWidth += marginLeft + marginRight + paddingLeft + paddingRight
//        }
//    }
}