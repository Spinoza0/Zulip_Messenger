package com.spinoza.messenger_tfs.presentation.feature.messages.ui

import android.content.Context
import android.text.Html
import android.text.Html.ImageGetter
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.MessageLayoutBinding
import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageDateTime
import com.spinoza.messenger_tfs.domain.model.ReactionParam
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.util.EMPTY_STRING
import com.spinoza.messenger_tfs.presentation.feature.messages.model.FlexBoxGravity
import com.spinoza.messenger_tfs.presentation.util.getAppComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MessageView @JvmOverloads constructor(
    context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
    private val defStyleRes: Int = 0,
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    var messageId: Long = Message.UNDEFINED_ID
        private set

    var userId: Long = User.UNDEFINED_ID
        private set

    var name: String
        get() = binding.nameTextView.text.toString()
        set(value) {
            binding.nameTextView.text = value
        }

    var rawContent = EMPTY_STRING

    val avatarImage: ImageView
        get() = binding.avatarImageView

    var datetime = MessageDateTime()

    var subject = EMPTY_STRING

    private val timePaddingEnd = TIME_PADDING_END.dpToPx(this).toInt()
    private val timePaddingBottom = TIME_PADDING_BOTTOM.dpToPx(this).toInt()
    private val ioDispatcher = context.getAppComponent().getDispatcherIO()
    private var imageJob: Job? = null

    private val imageGetter = ImageGetter { imageUrl ->
        val holder = DrawableHolder(resources)
        imageJob?.cancel()
        imageJob = CoroutineScope(ioDispatcher).launch {
            holder.loadImage(context, imageUrl, binding.contentTextView)
        }
        holder
    }

    private var content: String
        get() = binding.contentTextView.text.toString()
        set(value) {
            rawContent = value
            binding.contentTextView.text = Html.fromHtml(
                MessageTagHandler.prepareTag(value),
                Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL,
                imageGetter,
                MessageTagHandler()
            )
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
                    R.drawable.shape_message_user_bottom
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
        binding.contentTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        imageJob?.cancel()
        imageJob = null
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var offsetX = paddingLeft
        var offsetY = paddingTop
        var maxChildWidth = 0

        measureChildWithMargins(
            binding.timeTextView,
            widthMeasureSpec,
            offsetX,
            heightMeasureSpec,
            offsetY
        )

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
            binding.nameTextView.setMaxWidthUsingOffset(widthSize, offsetX)
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
        binding.contentTextView.setMaxWidthUsingOffset(widthSize, offsetX)
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
        with(binding) {
            val textWidth = maxOf(
                nameTextView.getWidthWithMargins(),
                contentTextView.getWidthWithMargins()
            )

            if (avatarImageView.isVisible) {
                avatarImageView.layoutWithMargins(offsetX, offsetY)
                offsetX += avatarImageView.getWidthWithMargins()
            }


            if (nameTextView.isVisible) {
                nameTextView.layoutWithMargins(offsetX, offsetY, textWidth)
                offsetY += nameTextView.getHeightWithMargins()
            }

            contentTextView.layoutWithMargins(offsetX, offsetY, textWidth)
            offsetY += contentTextView.getHeightWithMargins()
            timeTextView.layoutWithMargins(
                offsetX + textWidth - timeTextView.measuredWidth - timePaddingEnd,
                offsetY - timeTextView.measuredHeight - timePaddingBottom
            )

            if (reactionsGravity == FlexBoxGravity.END) {
                val reactionsWidth = reactionsFlexBoxLayout.getWidthWithMargins()
                if (reactionsWidth < textWidth) {
                    offsetX += textWidth - reactionsWidth
                }
            }
            reactionsFlexBoxLayout.layoutWithMargins(offsetX, offsetY)
        }
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
    }

    fun setOnReactionAddClickListener(listener: ((MessageView) -> Unit)?) {
        binding.reactionsFlexBoxLayout.setOnAddClickListener {
            listener?.invoke(this@MessageView)
        }
    }

    fun setOnReactionClickListener(listener: ((MessageView, ReactionView) -> Unit)?) {
        binding.reactionsFlexBoxLayout.setOnChildrenClickListener { _, view ->
            listener?.invoke(this@MessageView, view as ReactionView)
        }
    }

    fun setMessage(message: Message, reactionsGravity: FlexBoxGravity) {
        messageId = message.id
        userId = message.user.userId
        name = message.user.fullName
        content = message.content
        datetime = message.datetime
        subject = message.subject
        binding.timeTextView.text = datetime.timeString
        this.reactionsGravity = reactionsGravity
        setReactions(message.reactions)
    }

    fun setReactions(reactions: Map<Emoji, ReactionParam>) {
        binding.reactionsFlexBoxLayout.removeAllViews()
        binding.reactionsFlexBoxLayout.setIconAddVisibility(reactions.isNotEmpty())
        reactions.forEach {
            addReaction(it.key, it.value)
        }
    }

    private fun TextView.setMaxWidthUsingOffset(widthSize: Int, offset: Int) {
        this.maxWidth = widthSize - offset
    }

    private fun addReaction(reaction: Emoji, reactionParam: ReactionParam) {
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

    private companion object {

        const val REACTION_PADDING_HORIZONTAL = 10f
        const val REACTION_PADDING_VERTICAL = 7f
        const val TIME_PADDING_END = 14f
        const val TIME_PADDING_BOTTOM = 4f
    }
}