package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import com.spinoza.messenger_tfs.R

class MessageLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private val avatarImage: ImageView
    private val nameView: TextView
    private val messageView: TextView
    val reactionsGroup: FlexBoxLayout

    var avatarResId: Int = 0
        set(value) {
            field = value
            setAvatarParams()
        }

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

    private var offsetX = 0
    private var offsetY = 0

    var onReactionAddClickListener: (() -> Unit)? = null
        set(value) {
            field = value
            reactionsGroup.onIconAddClickListener = value
        }

    init {
        inflate(context, R.layout.message_layout, this)
        avatarImage = findViewById(R.id.avatar)
        nameView = findViewById(R.id.name)
        messageView = findViewById(R.id.message)
        reactionsGroup = findViewById(R.id.reactions)
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
        offsetX = marginLeft + paddingLeft
        offsetY = marginTop + paddingTop
        measureFunc?.let { measureFunc(avatarImage, widthMeasureSpec, heightMeasureSpec) }
        layoutFunc?.let { layoutFunc(avatarImage) }

        offsetX += avatarImage.measuredWidth
        measureFunc?.let { measureFunc(nameView, widthMeasureSpec, heightMeasureSpec) }
        layoutFunc?.let { layoutFunc(nameView) }

        var textWidth = nameView.measuredWidth
        offsetY += nameView.measuredHeight
        measureFunc?.let { measureFunc(messageView, widthMeasureSpec, heightMeasureSpec) }
        layoutFunc?.let { layoutFunc(messageView) }

        textWidth = maxOf(textWidth, messageView.measuredWidth)
        offsetY += messageView.measuredHeight
        measureFunc?.let {
            measureFunc(reactionsGroup, widthMeasureSpec, heightMeasureSpec)
        }
        layoutFunc?.let { layoutFunc(reactionsGroup) }

        measureFunc?.let {
            textWidth = maxOf(textWidth, reactionsGroup.measuredWidth)
            val totalWidth = offsetX + textWidth
            val totalHeight =
                offsetY + reactionsGroup.measuredHeight + marginBottom + paddingBottom
            setMeasuredDimension(totalWidth, totalHeight)
        }
    }

    private fun viewLayout(view: View) {
        view.layout(
            offsetX,
            offsetY,
            offsetX + view.measuredWidth,
            offsetY + view.measuredHeight
        )
    }

    private fun measureView(view: View, widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildWithMargins(view, widthMeasureSpec, offsetX, heightMeasureSpec, offsetY)
    }

    private fun setAvatarParams() {
        avatarImage.setImageResource(avatarResId)
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
}