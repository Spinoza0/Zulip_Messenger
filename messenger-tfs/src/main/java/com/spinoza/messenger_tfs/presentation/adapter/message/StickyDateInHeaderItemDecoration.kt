package com.spinoza.messenger_tfs.presentation.adapter.message

import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.presentation.adapter.message.date.DateDelegate

class StickyDateInHeaderItemDecoration : RecyclerView.ItemDecoration() {

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val topChild = parent.getChildAt(0) ?: return
        val topChildPosition = parent.getChildAdapterPosition(topChild)
        val header = parent.createHeaderView(topChildPosition)
            ?: parent.createPreviousHeaderView(topChildPosition)
            ?: return
        parent.makeViewFullLayout(header)
        val nextHeader = parent.findNextHeaderView()

        val dy = if (nextHeader != null) {
            val differ = nextHeader.top.toFloat() - header.height
            if (differ <= 0) differ else ZERO_HEIGHT
        } else {
            ZERO_HEIGHT
        }
        c.run {
            save()
            translate(ZERO_WIDTH, dy)
            header.draw(this)
            restore()
        }
    }

    private fun RecyclerView.createHeaderView(position: Int): View? {
        val adapter = adapter ?: return null
        val holder = adapter.onCreateViewHolder(
            this,
            adapter.getItemViewType(position)
        ) as? DateDelegate.ViewHolder ?: return null
        adapter.onBindViewHolder(holder, position)
        return holder.itemView
    }

    private fun RecyclerView.createPreviousHeaderView(position: Int): View? {
        if (position > START_POSITION) for (i in position downTo START_POSITION) {
            createHeaderView(i)?.let {
                return it
            }
        }
        return null
    }

    private fun RecyclerView.findNextHeaderView(): View? {
        val itemCount = adapter?.itemCount ?: NO_ITEMS
        if (itemCount > ONE_ITEM) {
            val visibleCount = layoutManager?.childCount ?: return null
            for (i in 1..visibleCount) {
                val header = getChildAt(i)
                if (header != null && getChildViewHolder(header) as? DateDelegate.ViewHolder != null) {
                    return header
                }
            }
        }
        return null
    }

    private fun RecyclerView.makeViewFullLayout(view: View) {
        val parentWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val parentHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

        val childLayoutParam = view.layoutParams ?: return

        val childWidth = ViewGroup.getChildMeasureSpec(
            parentWidth,
            0,
            childLayoutParam.width
        )
        val childHeight = ViewGroup.getChildMeasureSpec(
            parentHeight,
            0,
            childLayoutParam.height
        )

        view.measure(childWidth, childHeight)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    private companion object {
        const val START_POSITION = 0
        const val ZERO_WIDTH = 0f
        const val ZERO_HEIGHT = 0f
        const val NO_ITEMS = 0
        const val ONE_ITEM = 1
    }
}