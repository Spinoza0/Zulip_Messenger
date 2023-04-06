package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.util.TypedValue
import android.view.View
import androidx.core.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.OwnMessageDelegate
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.UserMessageDelegate

private const val MAX_DISTANCE = 5

fun Float.spToPx(view: View) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP,
    this,
    view.resources.displayMetrics
)

fun Float.dpToPx(view: View) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this,
    view.resources.displayMetrics
)

fun View.getHeightWithMargins(): Int {
    return this.measuredHeight + this.marginTop + this.marginBottom
}

fun View.getWidthWithMargins(): Int {
    return this.measuredWidth + this.marginLeft + this.marginRight
}

fun View.layoutWithMargins(offsetX: Int, offsetY: Int, minWidth: Int = this.measuredWidth) {
    val x = offsetX + this.marginLeft
    val y = offsetY + this.marginTop
    this.layout(x, y, x + maxOf(this.measuredWidth, minWidth), y + this.measuredHeight)
}

fun RecyclerView.smoothScrollToTargetPosition(targetPosition: Int) {
    val lastVisiblePosition = (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
    if ((targetPosition - lastVisiblePosition) > MAX_DISTANCE) {
        scrollToPosition(targetPosition - MAX_DISTANCE)
    }
    smoothScrollToPosition(targetPosition)
}

fun RecyclerView.smoothScrollToChangedMessage(changedMessageId: Long) {
    if (changedMessageId == Message.UNDEFINED_ID) return

    var position = RecyclerView.NO_POSITION
    for (i in 0 until this.childCount) {
        val messageView = this.getChildAt(i)
        val viewHolder = this.getChildViewHolder(messageView)
        var messageId = Message.UNDEFINED_ID
        if (viewHolder is OwnMessageDelegate.ViewHolder)
            messageId = viewHolder.binding.messageView.messageId
        else if (viewHolder is UserMessageDelegate.ViewHolder)
            messageId = viewHolder.binding.messageView.messageId
        if (messageId == changedMessageId) {
            position = viewHolder.adapterPosition
            break
        }
    }

    if (position != RecyclerView.NO_POSITION) {
        val layoutManager = layoutManager as LinearLayoutManager
        val firstCompletelyVisiblePosition =
            layoutManager.findFirstCompletelyVisibleItemPosition()
        val lastCompletelyVisiblePosition =
            layoutManager.findLastCompletelyVisibleItemPosition()
        if (position < firstCompletelyVisiblePosition ||
            position >= lastCompletelyVisiblePosition
        ) {
            smoothScrollToTargetPosition(position)
        }
    }
}

fun RecyclerView.smoothScrollToLastPosition() {
    val lastItemPosition = adapter?.itemCount?.minus(1)
    if (lastItemPosition != null) {
        smoothScrollToTargetPosition(lastItemPosition)
    }
}

fun Context.getThemeColor(attr: Int): Int {
    val typedValue = TypedValue()
    this.theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

fun ShimmerFrameLayout.on() {
    isVisible = true
    startShimmer()
}

fun ShimmerFrameLayout.off() {
    stopShimmer()
    isVisible = false
}