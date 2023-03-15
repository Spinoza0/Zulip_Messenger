package com.spinoza.messenger_tfs.presentation.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.MessageLayoutBinding
import com.spinoza.messenger_tfs.domain.model.FlexBoxGravity
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.ReactionParam

class MessageView @JvmOverloads constructor(
    context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
    private val defStyleRes: Int = 0,
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    var messageId: Int = 0
        private set

    var name: String
        get() = binding.nameTextView.text.toString()
        set(value) {
            binding.nameTextView.text = value
        }

    private var content: String
        get() = binding.contentTextView.text.toString()
        set(value) {
            binding.contentTextView.text = value
        }

    private val binding by lazy {
        MessageLayoutBinding.inflate(LayoutInflater.from(context), this)
    }

    private var reactionsGravity: FlexBoxGravity = FlexBoxGravity.START

    init {
        context.withStyledAttributes(attrs, R.styleable.message_view) {
            val gravity = getInt(
                R.styleable.message_view_reactions_gravity,
                FlexBoxGravity.START.ordinal
            )
            reactionsGravity = FlexBoxGravity.values()[gravity]

            val messageTypeIsUser = this.getBoolean(
                R.styleable.message_view_message_type_is_user,
                false
            )

            if (messageTypeIsUser) {
                val messageBackground = this.getResourceId(
                    R.styleable.message_view_message_background_color,
                    R.drawable.shape_message_companion_bottom
                )
                binding.contentTextView.setBackgroundResource(messageBackground)
                binding.avatarImageView.visibility = View.GONE
                binding.nameTextView.visibility = View.GONE
                binding.contentTextView.setPadding(
                    binding.contentTextView.paddingLeft,
                    binding.nameTextView.paddingTop,
                    binding.contentTextView.paddingRight,
                    binding.contentTextView.paddingBottom
                )
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var offsetX = paddingLeft
        var offsetY = paddingTop
        var maxChildWidth = 0
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
            maxChildWidth = binding.nameTextView.getWidthWithMargins()
            offsetY += binding.nameTextView.getHeightWithMargins()
        }

        measureChildWithMargins(
            binding.contentTextView,
            widthMeasureSpec,
            offsetX,
            heightMeasureSpec,
            offsetY
        )
        maxChildWidth = maxOf(maxChildWidth, binding.contentTextView.getWidthWithMargins())
        offsetY += binding.contentTextView.getHeightWithMargins()

        measureChildWithMargins(
            binding.reactionsFlexBoxLayout,
            widthMeasureSpec,
            offsetX,
            heightMeasureSpec,
            offsetY
        )
        offsetY += binding.reactionsFlexBoxLayout.getHeightWithMargins()
        maxChildWidth = maxOf(maxChildWidth, binding.reactionsFlexBoxLayout.getWidthWithMargins())

        val totalWidth = offsetX + maxChildWidth
        val totalHeight = offsetY + paddingBottom
        setMeasuredDimension(totalWidth, totalHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var offsetX = paddingLeft
        var offsetY = paddingTop
        val textWidth = maxOf(
            binding.nameTextView.getWidthWithMargins(),
            binding.contentTextView.getWidthWithMargins()
        )

        if (binding.avatarImageView.isVisible) {
            binding.avatarImageView.layoutWithMargins(offsetX, offsetY)
            offsetX += binding.avatarImageView.getWidthWithMargins()
        }


        if (binding.nameTextView.isVisible) {
            binding.nameTextView.layoutWithMargins(offsetX, offsetY, textWidth)
            offsetY += binding.nameTextView.getHeightWithMargins()
        }

        binding.contentTextView.layoutWithMargins(offsetX, offsetY, textWidth)
        offsetY += binding.contentTextView.getHeightWithMargins()

        if (reactionsGravity == FlexBoxGravity.END) {
            val reactionsWidth = binding.reactionsFlexBoxLayout.getWidthWithMargins()
            if (reactionsWidth < textWidth) {
                offsetX += textWidth - reactionsWidth
            }
        }
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

    private fun setRoundAvatar(resId: Int) {
        val bitmap = BitmapFactory.decodeResource(resources, resId)
        setRoundAvatar(bitmap)
    }

    private fun setRoundAvatar(bitmap: Bitmap) {
        val size = binding.avatarImageView.layoutParams.width.toFloat().dpToPx(this)
        binding.avatarImageView.setImageBitmap(bitmap.getRounded(size))
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
        binding.contentTextView.setOnLongClickListener {
            listener?.invoke(this@MessageView)
            true
        }
        binding.reactionsFlexBoxLayout.setOnAddClickListener {
            listener?.invoke(this@MessageView)
        }
    }

    fun setOnReactionClickListener(listener: ((MessageView, ReactionView) -> Unit)?) {
        binding.reactionsFlexBoxLayout.setOnChildrenClickListener { _, view ->
            listener?.invoke(this@MessageView, view as ReactionView)
        }
    }

    private fun setIconAddVisibility(state: Boolean) {
        binding.reactionsFlexBoxLayout.setIconAddVisibility(state)
    }

    private fun addReaction(reaction: String, reactionParam: ReactionParam) {
        val reactionView =
            ReactionView(context, attrs, defStyleAttr, defStyleRes).apply {
                emoji = reaction
                count = reactionParam.count
                isSelected = reactionParam.isSelected
                isCountVisible = count > 1
                setCustomPadding(
                    REACTION_PADDING_HORIZONTAL,
                    REACTION_PADDING_VERTICAL,
                    REACTION_PADDING_HORIZONTAL,
                    REACTION_PADDING_VERTICAL,
                )
            }
        binding.reactionsFlexBoxLayout.addView(reactionView)
    }

    fun setMessage(message: Message, reactionsGravity: FlexBoxGravity) {
        messageId = message.id
        name = message.name
        content = message.content
        this.reactionsGravity = reactionsGravity
        setRoundAvatar(message.avatarResId)
        setIconAddVisibility(message.isIconAddVisible)
        binding.reactionsFlexBoxLayout.removeAllViews()
        message.reactions.forEach {
            addReaction(it.key, it.value)
        }
    }

    private companion object {
        const val REACTION_PADDING_HORIZONTAL = 10f
        const val REACTION_PADDING_VERTICAL = 7f
    }
}