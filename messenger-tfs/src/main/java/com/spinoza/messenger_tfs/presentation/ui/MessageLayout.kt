package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.dpToPx
import com.spinoza.messenger_tfs.getRoundImage

class MessageLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private val avatar: ImageView
    private val nameView: TextView
    private val messageView: TextView
    val reactions: FlexBoxLayout

    var name: String = ""
        set(value) {
            field = value
            nameView.text = value
        }

    var message: String = ""
        set(value) {
            field = value
            messageView.text = value
        }

    private var cursor = CursorView()

    var onReactionAddClickListener: (() -> Unit)? = null
        set(value) {
            field = value
            reactions.onIconAddClickListener = value
        }

    init {
        inflate(context, R.layout.message_layout, this)
        avatar = findViewById(R.id.avatar)
        nameView = findViewById(R.id.name)
        messageView = findViewById(R.id.message)
        reactions = findViewById(R.id.reactions)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        processLayout(
            widthMeasureSpec = widthMeasureSpec,
            heightMeasureSpec = heightMeasureSpec,
            measureFunc = ::measureView
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        processLayout(layoutFunc = ::viewLayout)
    }

    private fun processLayout(
        widthMeasureSpec: Int = 0,
        heightMeasureSpec: Int = 0,
        measureFunc: ((View, Int, Int) -> Unit)? = null,
        layoutFunc: ((View) -> Unit)? = null,
    ) {
        cursor.reset(marginLeft + paddingLeft, marginTop + paddingTop)

        measureFunc?.let { measureFunc(avatar, widthMeasureSpec, heightMeasureSpec) }
        layoutFunc?.let { layoutFunc(avatar) }

        cursor.right(avatar.marginLeft + avatar.measuredWidth + avatar.marginRight)
        measureFunc?.let { measureFunc(nameView, widthMeasureSpec, heightMeasureSpec) }
        layoutFunc?.let { layoutFunc(nameView) }

        var textWidth = nameView.marginLeft + nameView.measuredWidth + nameView.marginRight
        cursor.down(nameView.marginTop + nameView.measuredHeight + nameView.marginBottom)
        measureFunc?.let { measureFunc(messageView, widthMeasureSpec, heightMeasureSpec) }
        layoutFunc?.let { layoutFunc(messageView) }

        textWidth = maxOf(
            textWidth,
            messageView.marginLeft + messageView.measuredWidth + messageView.marginRight
        )
        cursor.down(
            messageView.marginTop + messageView.measuredHeight + messageView.marginBottom
        )
        measureFunc?.let { measureFunc(reactions, widthMeasureSpec, heightMeasureSpec) }
        layoutFunc?.let { layoutFunc(reactions) }

        measureFunc?.let {
            cursor.down(
                reactions.marginTop + reactions.measuredHeight + reactions.marginBottom
            )
            textWidth = maxOf(
                textWidth,
                reactions.marginLeft + reactions.measuredWidth + reactions.marginRight
            )
            val totalWidth = cursor.x + textWidth
            val totalHeight = cursor.y + marginBottom + paddingBottom
            setMeasuredDimension(totalWidth, totalHeight)
        }
    }

    private fun viewLayout(view: View) {
        cursor.right(view.marginLeft)
        cursor.down(view.marginTop)
        view.layout(
            cursor.x,
            cursor.y,
            cursor.x + view.measuredWidth,
            cursor.y + view.measuredHeight
        )
        cursor.left(view.marginLeft)
        cursor.up(view.marginTop)
    }

    private fun measureView(view: View, widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildWithMargins(view, widthMeasureSpec, cursor.x, heightMeasureSpec, cursor.y)
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

    fun setAvatar(resId: Int) {
        avatar.setImageResource(resId)
    }

    fun setAvatar(bitmap: Bitmap) {
        avatar.setImageBitmap(bitmap)
    }

    fun setRoundAvatar(resId: Int) {
        val bitmap = BitmapFactory.decodeResource(resources, resId)
        setRoundAvatar(bitmap)
    }

    fun setRoundAvatar(bitmap: Bitmap) {
        val size = avatar.layoutParams.width.toFloat().dpToPx(this)
        setAvatar(getRoundImage(bitmap, size))
    }

    private inner class CursorView {
        private var _x = 0
        val x: Int
            get() = _x

        private var _y = 0
        val y: Int
            get() = _y

        fun reset(x: Int, y: Int) {
            _x = x
            _y = y
        }

        fun left(offset: Int) {
            _x -= offset
        }

        fun right(offset: Int) {
            _x += offset
        }

        fun up(offset: Int) {
            _y -= offset
        }

        fun down(offset: Int) {
            _y += offset
        }
    }
}