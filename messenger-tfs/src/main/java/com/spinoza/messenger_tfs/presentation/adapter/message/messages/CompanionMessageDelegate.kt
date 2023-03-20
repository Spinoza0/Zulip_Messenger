package com.spinoza.messenger_tfs.presentation.adapter.message.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.CompanionMessageItemBinding
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.presentation.adapter.message.AdapterDelegate
import com.spinoza.messenger_tfs.presentation.adapter.message.DelegateItem

class CompanionMessageDelegate : AdapterDelegate {

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CompanionMessageItemBinding.inflate(
            inflater,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        item: DelegateItem,
        position: Int,
    ) {
        (holder as ViewHolder).bind(item as CompanionMessageDelegateItem)
    }

    override fun isOfViewType(item: DelegateItem): Boolean {
        return item is CompanionMessageDelegateItem
    }

    class ViewHolder(val binding: CompanionMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MessageDelegateItem) {
            with(binding.messageView) {
                val message = item.content() as Message
                setMessage(message, item.getGravity())
                Glide.with(avatarImage)
                    .load(message.user.avatar_url)
                    .circleCrop()
                    .error(R.drawable.ic_default_avatar)
                    .into(avatarImage)
                setOnAvatarClickListener(item.getAvatarClickListener())
                setOnMessageLongClickListener(item.getReactionAddClickListener())
                setOnReactionClickListener(item.getReactionClickListener())
            }
        }
    }
}