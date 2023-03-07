package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.MessageEntity

class MessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    var name: String
        get() = nameTextView.text.toString()
        set(value) {
            nameTextView.text = value
        }

    var text: String
        get() = messageTextView.text.toString()
        set(value) {
            messageTextView.text = value
        }

    private val nameTextView: TextView
    private val messageTextView: TextView
    private val reactions: FlexBoxLayout
    private val avatar: ImageView

    init {
        inflate(context, R.layout.message_layout, this)
        avatar = findViewById(R.id.avatar)
        nameTextView = findViewById(R.id.name)
        messageTextView = findViewById(R.id.message)
        reactions = findViewById(R.id.reactions)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var offsetX = paddingLeft
        var offsetY = paddingTop
        measureChildWithMargins(avatar, widthMeasureSpec, offsetX, heightMeasureSpec, offsetY)
        offsetX += avatar.getWidthWithMargins()

        measureChildWithMargins(nameTextView, widthMeasureSpec, offsetX, heightMeasureSpec, offsetY)
        var textWidth = nameTextView.getWidthWithMargins()
        offsetY += nameTextView.getHeightWithMargins()

        measureChildWithMargins(
            messageTextView,
            widthMeasureSpec,
            offsetX,
            heightMeasureSpec,
            offsetY
        )
        textWidth = maxOf(textWidth, messageTextView.getWidthWithMargins())
        offsetY += messageTextView.getHeightWithMargins()

        measureChildWithMargins(reactions, widthMeasureSpec, offsetX, heightMeasureSpec, offsetY)
        offsetY += reactions.getHeightWithMargins()
        textWidth = maxOf(textWidth, reactions.getWidthWithMargins())

        val totalWidth = offsetX + textWidth
        val totalHeight = offsetY + paddingBottom
        setMeasuredDimension(totalWidth, totalHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var offsetX = paddingLeft
        var offsetY = paddingTop
        var offsetXWithMargin = offsetX + avatar.marginLeft
        var offsetYWithMargin = offsetY + avatar.marginTop
        avatar.layout(
            offsetXWithMargin,
            offsetYWithMargin,
            offsetXWithMargin + avatar.measuredWidth,
            offsetYWithMargin + avatar.measuredHeight
        )
        offsetX += avatar.getWidthWithMargins()

        offsetXWithMargin = offsetX + nameTextView.marginLeft
        offsetYWithMargin = offsetY + nameTextView.marginTop
        nameTextView.layout(
            offsetXWithMargin,
            offsetYWithMargin,
            offsetXWithMargin + nameTextView.measuredWidth,
            offsetYWithMargin + nameTextView.measuredHeight
        )
        offsetY += nameTextView.getHeightWithMargins()

        offsetXWithMargin = offsetX + messageTextView.marginLeft
        offsetYWithMargin = offsetY + messageTextView.marginTop
        messageTextView.layout(
            offsetXWithMargin,
            offsetYWithMargin,
            offsetXWithMargin + messageTextView.measuredWidth,
            offsetYWithMargin + messageTextView.measuredHeight
        )
        offsetY += messageTextView.getHeightWithMargins()

        offsetXWithMargin = offsetX + reactions.marginLeft
        offsetYWithMargin = offsetY + reactions.marginTop
        reactions.layout(
            offsetXWithMargin,
            offsetYWithMargin,
            offsetXWithMargin + reactions.measuredWidth,
            offsetYWithMargin + reactions.measuredHeight
        )
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
        setAvatar(bitmap.getRounded(size))
    }

    fun setOnAvatarClickListener(listener: ((MessageView) -> Unit)?) {
        avatar.setOnClickListener {
            listener?.invoke(this@MessageView)
        }
    }

    fun setOnMessageClickListener(listener: ((MessageView) -> Unit)?) {
        nameTextView.setOnClickListener {
            listener?.invoke(this@MessageView)
        }
        messageTextView.setOnClickListener {
            listener?.invoke(this@MessageView)
        }
    }

    fun setOnReactionAddClickListener(listener: ((MessageView) -> Unit)?) {
        reactions.setOnAddClickListener {
            listener?.invoke(this@MessageView)
        }
    }

    fun setIconAddVisibility(state: Boolean) {
        reactions.setIconAddVisibility(state)
    }

    fun addReactionView(reaction: ReactionView) {
        reactions.addView(reaction)
    }

    fun getMessageEntity(): MessageEntity {
        return MessageEntity(
            name,
            text,
            reactions.getReactionEntities(),
            reactions.getIconAddVisibility()
        )
    }

    fun setMessage(messageEntity: MessageEntity) {
        name = messageEntity.name
        text = messageEntity.text
        reactions.setIconAddVisibility(messageEntity.iconAddVisibility)
        reactions.setReactions(messageEntity.reactions)
    }
}