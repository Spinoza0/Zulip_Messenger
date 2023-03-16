package com.spinoza.messenger_tfs.presentation.adapter.delegate.channel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.databinding.ChannelTopicItemBinding
import com.spinoza.messenger_tfs.databinding.ChannelUnfoldedItemBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.presentation.adapter.delegate.AdapterDelegate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateItem

class ChannelUnfoldedDelegate : AdapterDelegate {

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return ViewHolder(
            ChannelUnfoldedItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        item: DelegateItem,
        position: Int,
    ) {
        (holder as ViewHolder).bind(item as ChannelUnfoldedDelegateItem)
    }

    override fun isOfViewType(item: DelegateItem): Boolean {
        return item is ChannelUnfoldedDelegateItem
    }

    class ViewHolder(private val binding: ChannelUnfoldedItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChannelUnfoldedDelegateItem) {
            val channel = item.content() as Channel
            binding.root.setOnClickListener {
                item.getOnChannelClickListener().invoke(channel.channelId)
            }
            binding.textViewChannel.text = channel.name
            binding.linearLayoutTopics.removeAllViews()
            channel.topics.forEach { topic ->
                val topicBinding = ChannelTopicItemBinding.inflate(
                    LayoutInflater.from(itemView.context),
                    binding.root,
                    false
                )
                topicBinding.textViewTopic.text = topic.name
                binding.linearLayoutTopics.addView(topicBinding.textViewTopic)
            }
        }
    }
}