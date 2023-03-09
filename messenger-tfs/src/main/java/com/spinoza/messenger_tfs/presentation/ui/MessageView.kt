package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.*
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.MessageLayoutBinding
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageType
import com.spinoza.messenger_tfs.domain.model.Reaction

class MessageView @JvmOverloads constructor(
    context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
    private val defStyleRes: Int = 0,
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private val binding by lazy {
        MessageLayoutBinding.inflate(LayoutInflater.from(context), this)
    }

    private val messageViewOriginalMarginLeft: Int = marginLeft
    private val messageViewOriginalMarginRight: Int = marginRight
    private val messageTextViewOriginalPaddingTop: Int = binding.messageTextView.paddingTop

    var messageId: Int = 0

    var name: String
        get() = binding.nameTextView.text.toString()
        set(value) {
            binding.nameTextView.text = value
        }

    var text: String
        get() = binding.messageTextView.text.toString()
        set(value) {
            binding.messageTextView.text = value
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var offsetX = paddingLeft
        var offsetY = paddingTop
        var textWidth = 0
        if (binding.avatarImageView.isVisible) {
            measureChildWithMargins(
                binding.avatarImageView,
                widthMeasureSpec,
                offsetX,
                heightMeasureSpec,
                offsetY
            )
            offsetX += binding.avatarImageView.getWidthWithMargins()
        }

        if (binding.nameTextView.isVisible) {
            measureChildWithMargins(
                binding.nameTextView,
                widthMeasureSpec,
                offsetX,
                heightMeasureSpec,
                offsetY
            )
            textWidth = binding.nameTextView.getWidthWithMargins()
            offsetY += binding.nameTextView.getHeightWithMargins()
        }

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
        if (binding.avatarImageView.isVisible) {
            binding.avatarImageView.layoutWithMargins(offsetX, offsetY)
            offsetX += binding.avatarImageView.getWidthWithMargins()
        }

        if (binding.nameTextView.isVisible) {
            binding.nameTextView.layoutWithMargins(offsetX, offsetY)
            offsetY += binding.nameTextView.getHeightWithMargins()
        }

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
        messageId = message.id
        name = message.user.name
        text = message.text
        setRoundAvatar(message.user.avatarResId)
        setIconAddVisibility(message.iconAddVisibility)
        message.reactions.keys.forEach {
            addReaction(it)
        }
    }

    fun setMessageType(messageType: MessageType) {
        when (messageType) {
            MessageType.USER -> {
                binding.avatarImageView.visibility = View.GONE
                binding.nameTextView.visibility = View.GONE
                (layoutParams as MarginLayoutParams).setMargins(
                    marginLeft + OFFSET_MARGIN_LEFT.dpToPx(this).toInt(),
                    marginTop,
                    messageViewOriginalMarginRight,
                    marginBottom
                )
                with(binding.messageTextView) {
                    setBackgroundResource(R.drawable.shape_message_user)
                    setPadding(
                        paddingLeft,
                        binding.nameTextView.paddingTop,
                        paddingRight,
                        paddingBottom
                    )
                }
            }
            MessageType.COMPANION -> {
                binding.avatarImageView.visibility = View.VISIBLE
                binding.nameTextView.visibility = View.VISIBLE
                (layoutParams as MarginLayoutParams).setMargins(
                    messageViewOriginalMarginLeft,
                    marginTop,
                    marginRight + OFFSET_MARGIN_RIGHT.dpToPx(this).toInt(),
                    marginBottom
                )
                (layoutParams as MarginLayoutParams).setMargins(
                    messageViewOriginalMarginLeft,
                    marginTop,
                    marginRight + OFFSET_MARGIN_RIGHT.dpToPx(this).toInt(),
                    marginBottom
                )
                with(binding.messageTextView) {
                    setBackgroundResource(
                        R.drawable.shape_message_companion_bottom
                    )
                    setPadding(
                        paddingLeft,
                        messageTextViewOriginalPaddingTop,
                        paddingRight,
                        paddingBottom
                    )
                }
            }
        }
    }

    private companion object {
        const val OFFSET_MARGIN_LEFT = 150f
        const val OFFSET_MARGIN_RIGHT = 100f
    }
}