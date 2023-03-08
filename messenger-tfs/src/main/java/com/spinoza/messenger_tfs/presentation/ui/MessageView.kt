package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import com.spinoza.messenger_tfs.databinding.MessageLayoutBinding
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.Reaction
import com.spinoza.messenger_tfs.domain.model.User

class MessageView @JvmOverloads constructor(
    context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
    private val defStyleRes: Int = 0,
    user: User,
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private val binding by lazy {
        MessageLayoutBinding.inflate(LayoutInflater.from(context), this)
    }

    var user: User = User("","")
        set(value) {
            field = value
            binding.nameTextView.text = value.name
        }

    var text: String
        get() = binding.messageTextView.text.toString()
        set(value) {
            binding.messageTextView.text = value
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var offsetX = paddingLeft
        var offsetY = paddingTop
        measureChildWithMargins(
            binding.avatarImageView,
            widthMeasureSpec,
            offsetX,
            heightMeasureSpec,
            offsetY
        )
        offsetX += binding.avatarImageView.getWidthWithMargins()

        measureChildWithMargins(
            binding.nameTextView,
            widthMeasureSpec,
            offsetX,
            heightMeasureSpec,
            offsetY
        )
        var textWidth = binding.nameTextView.getWidthWithMargins()
        offsetY += binding.nameTextView.getHeightWithMargins()

        measureChildWithMargins(
            binding.messageTextView,
            widthMeasureSpec,
            offsetX,
            heightMeasureSpec,
            offsetY
        )
        textWidth = maxOf(textWidth, binding.messageTextView.getWidthWithMargins())
        offsetY += binding.messageTextView.getHeightWithMargins()

        measureChildWithMargins(
            binding.reactionsFlexBoxLayout,
            widthMeasureSpec,
            offsetX,
            heightMeasureSpec,
            offsetY
        )
        offsetY += binding.reactionsFlexBoxLayout.getHeightWithMargins()
        textWidth = maxOf(textWidth, binding.reactionsFlexBoxLayout.getWidthWithMargins())

        val totalWidth = offsetX + textWidth
        val totalHeight = offsetY + paddingBottom
        setMeasuredDimension(totalWidth, totalHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var offsetX = paddingLeft
        var offsetY = paddingTop
        binding.avatarImageView.layoutWithMargins(offsetX, offsetY)
        offsetX += binding.avatarImageView.getWidthWithMargins()

        binding.nameTextView.layoutWithMargins(offsetX, offsetY)
        offsetY += binding.nameTextView.getHeightWithMargins()

        binding.messageTextView.layoutWithMargins(offsetX, offsetY)
        offsetY += binding.messageTextView.getHeightWithMargins()

        binding.reactionsFlexBoxLayout.layoutWithMargins(offsetX, offsetY)
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
        binding.avatarImageView.setImageResource(resId)
    }

    fun setAvatar(bitmap: Bitmap) {
        binding.avatarImageView.setImageBitmap(bitmap)
    }

    fun setRoundAvatar(resId: Int) {
        val bitmap = BitmapFactory.decodeResource(resources, resId)
        setRoundAvatar(bitmap)
    }

    fun setRoundAvatar(bitmap: Bitmap) {
        val size = binding.avatarImageView.layoutParams.width.toFloat().dpToPx(this)
        setAvatar(bitmap.getRounded(size))
    }

    fun setOnAvatarClickListener(listener: ((MessageView) -> Unit)?) {
        binding.avatarImageView.setOnClickListener {
            listener?.invoke(this@MessageView)
        }
    }

    fun setOnMessageLongClickListener(listener: ((MessageView) -> Unit)?) {
        binding.nameTextView.setOnLongClickListener {
            listener?.invoke(this@MessageView)
            true
        }
        binding.messageTextView.setOnLongClickListener {
            listener?.invoke(this@MessageView)
            true
        }
    }

    fun setOnReactionAddClickListener(listener: ((MessageView) -> Unit)?) {
        binding.reactionsFlexBoxLayout.setOnAddClickListener {
            listener?.invoke(this@MessageView)
        }
    }

    fun setIconAddVisibility(state: Boolean) {
        binding.reactionsFlexBoxLayout.setIconAddVisibility(state)
    }

    fun addReaction(reaction: Reaction) {
        val reactionView = ReactionView(context, attrs, defStyleAttr, defStyleRes).apply {
            emoji = reaction.emoji
            count = reaction.count
            isSelected = reaction.isSelected
        }
        binding.reactionsFlexBoxLayout.addView(reactionView)
    }

    fun setMessage(message: Message) {
        user = message.user
        text = message.text
        binding.reactionsFlexBoxLayout.setIconAddVisibility(message.iconAddVisibility)
        message.reactions.keys.forEach {
            addReaction(it)
        }
    }
}