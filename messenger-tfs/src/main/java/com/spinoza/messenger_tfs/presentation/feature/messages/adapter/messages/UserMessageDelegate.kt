package com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.UserMessageItemBinding
import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.ReactionParam
import com.spinoza.messenger_tfs.presentation.feature.app.adapter.AdapterDelegate
import com.spinoza.messenger_tfs.presentation.feature.app.adapter.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.MessageView
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.ReactionView

class UserMessageDelegate(
    private val onMessageLongClickListener: (MessageView) -> Unit,
    private val onReactionAddClickListener: (MessageView) -> Unit,
    private val onReactionClickListener: (MessageView, ReactionView) -> Unit,
    private val onAvatarClickListener: (MessageView) -> Unit,
) : AdapterDelegate {

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = UserMessageItemBinding.inflate(
            inflater,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        item: DelegateAdapterItem,
        position: Int,
    ) {
        (holder as ViewHolder).bind(
            item as UserMessageDelegateItem,
            onMessageLongClickListener,
            onReactionAddClickListener,
            onReactionClickListener,
            onAvatarClickListener,
        )
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        item: DelegateAdapterItem,
        position: Int,
        payloads: List<Any>,
    ) {
        if (payloads.isEmpty() || (payloads[0] as? Map<*, *>) == null) {
            onBindViewHolder(holder, item, position)
        } else {
            (holder as ViewHolder).bind(payloads[0] as Map<*, *>)
        }
    }

    override fun isOfViewType(item: DelegateAdapterItem): Boolean {
        return item is UserMessageDelegateItem
    }

    class ViewHolder(val binding: UserMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: UserMessageDelegateItem,
            onMessageLongClickListener: (MessageView) -> Unit,
            onReactionAddClickListener: (MessageView) -> Unit,
            onReactionClickListener: (MessageView, ReactionView) -> Unit,
            onAvatarClickListener: ((MessageView) -> Unit)?,
        ) {
            with(binding.messageView) {
                val message = item.content() as Message
                setMessage(message, item.getGravity())
                Glide.with(avatarImage)
                    .load(message.user.avatarUrl)
                    .circleCrop()
                    .error(R.drawable.ic_default_avatar)
                    .into(avatarImage)
                setReactions(message.reactions)
                setOnAvatarClickListener(onAvatarClickListener)
                setOnMessageLongClickListener(onMessageLongClickListener)
                setOnReactionAddClickListener(onReactionAddClickListener)
                setOnReactionClickListener(onReactionClickListener)
            }
        }

        fun bind(payloadMap: Map<*, *>) {
            val reactions = payloadMap.entries
                .filterIsInstance<Map.Entry<Emoji, ReactionParam>>()
                .associateBy({ it.key }, { it.value })
            binding.messageView.setReactions(reactions)
        }
    }
}