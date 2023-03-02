package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.*
import kotlin.math.max

class FlexBoxLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ViewGroup(context, attrs, defStyleAttr) {

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

    // TODO: zero margin support
    private fun processChildren(
        layoutWidth: Int,
        parentWidthMeasureSpec: Int,
        parentHeightMeasureSpec: Int,
        prepareChildSize: ((View, Int, Int, Int, Int) -> Unit)? = null,
        setChildDimension: ((Int, Int) -> Unit)? = null,
        childLayout: ((View, Int, Int, Int, Int) -> Unit)? = null,
    ) {
        val leftDeltaX = marginLeft + paddingLeft
        val rightDeltaX = marginRight + paddingRight
        val startDeltaY = marginTop + paddingTop

        var offsetX = leftDeltaX
        var offsetY = startDeltaY
        var rowHeight = 0

        var maxWidth = 0
        var maxHeight = 0

        children.forEach { child ->
            if (child.visibility != View.GONE) {
                prepareChildSize?.let {
                    prepareChildSize(
                        child,
                        parentWidthMeasureSpec,
                        offsetX,
                        parentHeightMeasureSpec,
                        offsetY
                    )
                }
                val childWidth = getChildWidth(child)
                val childHeight = getChildHeight(child)

                if (offsetX + childWidth + rightDeltaX > layoutWidth) {
                    offsetX = leftDeltaX
                    val deltaY = rowHeight + marginTop
                    offsetY += deltaY
                    maxHeight += deltaY
                    rowHeight = childHeight
                } else {
                    rowHeight = max(rowHeight, childHeight)
                }

                childLayout?.let {
                    childLayout(
                        child,
                        offsetX,
                        offsetY,
                        offsetX + childWidth,
                        offsetY + childHeight
                    )
                }
                offsetX += childWidth + marginRight
                maxWidth = max(maxWidth, offsetX)
            }
        }

        setChildDimension?.let {
            maxHeight +=
                rowHeight + marginTop + paddingTop + paddingBottom + paddingTop + paddingBottom
            maxWidth += paddingLeft + paddingRight + marginLeft + marginRight
            val width = resolveSize(maxWidth, parentWidthMeasureSpec)
            val height = resolveSize(maxHeight, parentHeightMeasureSpec)
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

    private companion object {
        const val MARGIN_VERTICAL = 7f
        const val MARGIN_HORIZONTAL = 10f
    }
}