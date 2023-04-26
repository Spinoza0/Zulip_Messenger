package com.spinoza.messenger_tfs.presentation.feature.messages.ui

import android.content.Context
import android.util.TypedValue
import android.view.View
import androidx.core.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.spinoza.messenger_tfs.BuildConfig
import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.presentation.feature.app.adapter.MainDelegateAdapter
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages.OwnMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages.UserMessageDelegateItem

private const val MAX_DISTANCE = 5
private const val EMOJI_BASE = 16
private const val FIRST_PART = 0

fun Emoji.toCharacterImage(): String {
    return runCatching {
        val codeParts = code.split("-").map { it.toInt(EMOJI_BASE) }.toIntArray()
        String(codeParts, FIRST_PART, codeParts.size)
    }.getOrElse { ":$name:" }
}

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
    if (targetPosition == RecyclerView.NO_POSITION) return
    val lastVisiblePosition = (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
    if ((targetPosition - lastVisiblePosition) > MAX_DISTANCE) {
        scrollToPosition(targetPosition - MAX_DISTANCE)
    }
    smoothScrollToPosition(targetPosition)
}

fun RecyclerView.smoothScrollToMessage(messageId: Long) {
    if (messageId == Message.UNDEFINED_ID) return
    var position = RecyclerView.NO_POSITION
    val delegateAdapter = adapter as MainDelegateAdapter
    for (index in 0 until delegateAdapter.itemCount) {
        val item = delegateAdapter.getItem(index)
        if (item is UserMessageDelegateItem || item is OwnMessageDelegateItem) {
            if ((item.content() as Message).id == messageId) {
                position = index
                break
            }
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

fun String.getFullUrl(): String {
    return if (this.startsWith(URL_SECURED_PREFIX, ignoreCase = true) ||
        this.startsWith(URL_BASIC_PREFIX, ignoreCase = true)
    ) {
        this
    } else {
        "${BASE_URL}$this"
    }
}

fun String.isUserUploadsUrl(): Boolean {
    return this.startsWith(URL_USER_UPLOADS_PREFIX, ignoreCase = true)
}

private const val BASE_URL = BuildConfig.ZULIP_SERVER_URL
private const val URL_SECURED_PREFIX = "https://"
private const val URL_BASIC_PREFIX = "http://"
private const val URL_USER_UPLOADS_PREFIX = "$BASE_URL/user_uploads"