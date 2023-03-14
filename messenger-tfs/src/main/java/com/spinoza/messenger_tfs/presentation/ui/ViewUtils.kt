package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.model.Message

private const val MAX_DISTANCE = 10

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

fun smoothScrollToPosition(recyclerView: RecyclerView, lastPosition: Int) {
    val lastVisiblePosition =
        (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

    if (lastVisiblePosition != RecyclerView.NO_POSITION &&
        (lastPosition - lastVisiblePosition) > MAX_DISTANCE
    ) {
        recyclerView.scrollToPosition(lastPosition - MAX_DISTANCE)
    }
    recyclerView.smoothScrollToPosition(lastPosition)
}

fun smoothScrollToChangedMessage(recyclerView: RecyclerView, changedMessageId: Int) {
    if (changedMessageId == Message.UNDEFINED_ID) return

    var position = RecyclerView.NO_POSITION
    for (i in 0 until recyclerView.childCount) {
        val messageView = recyclerView.getChildAt(i)
        val item = messageView.findViewById<MessageView>(R.id.messageView)
        if (item != null && item.messageId == changedMessageId) {
            position = recyclerView.getChildAdapterPosition(messageView)
            break
        }
    }

    if (position != RecyclerView.NO_POSITION) {
        val layoutManager = (recyclerView.layoutManager as LinearLayoutManager)
        val firstCompletelyVisiblePosition =
            layoutManager.findFirstCompletelyVisibleItemPosition()
        val lastCompletelyVisiblePosition =
            layoutManager.findLastCompletelyVisibleItemPosition()
        if (position < firstCompletelyVisiblePosition ||
            position >= lastCompletelyVisiblePosition
        ) {
            smoothScrollToPosition(recyclerView, position)
        }
    }
}

fun Context.getThemeColor(attr: Int): Int {
    val typedValue = TypedValue()
    this.theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

fun Bitmap.getRounded(size: Float): Bitmap {
    val result = Bitmap.createBitmap(size.toInt(), size.toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    val path = Path()
    path.addCircle((size - 1) / 2, (size - 1) / 2, size / 2, Path.Direction.CCW)
    canvas.clipPath(path)
    canvas.drawBitmap(
        this,
        Rect(0, 0, this.width, this.height),
        Rect(0, 0, size.toInt(), size.toInt()), null
    )
    return result
}