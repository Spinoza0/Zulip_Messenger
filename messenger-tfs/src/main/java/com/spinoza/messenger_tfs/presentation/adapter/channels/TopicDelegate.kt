package com.spinoza.messenger_tfs.presentation.adapter.channels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.databinding.TopicItemBinding
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.presentation.adapter.delegate.AdapterDelegate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem

class TopicDelegate(
    private val evenColor: Int,
    private val oddColor: Int,
    private val onClickListener: (MessagesFilter) -> Unit,
) : AdapterDelegate {

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TopicItemBinding.inflate(
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
            item as TopicDelegateItem,
            position,
            evenColor,
            oddColor,
            onClickListener
        )
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        item: DelegateAdapterItem,
        position: Int,
        payloads: List<Any>,
    ) {
        if (payloads.isEmpty() || (payloads[0] as? Int) == null) {
            onBindViewHolder(holder, item, position)
        } else {
            (holder as ViewHolder).bind(payloads[0] as Int)
        }
    }

    override fun isOfViewType(item: DelegateAdapterItem): Boolean {
        return item is TopicDelegateItem
    }

    class ViewHolder(private val binding: TopicItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: TopicDelegateItem,
            position: Int,
            evenColor: Int,
            oddColor: Int,
            onClickListener: (MessagesFilter) -> Unit,
        ) {
            val color = if (position % 2 == 0) evenColor else oddColor
            val messagesFilter = (item.content() as MessagesFilter)
            binding.textViewTopicLayout.setBackgroundColor(color)
            binding.textViewTopicName.text = messagesFilter.topic.name
            binding.root.setOnClickListener {
                onClickListener(messagesFilter)
            }
            bind(messagesFilter.topic.messageCount)
        }

        fun bind(messageCount: Int) {
            binding.textViewTopicMessagesCount.text = messageCount.toString()
            binding.textViewTopicMessagesCount.isVisible = messageCount > 0
            binding.textViewTopicMessagesCountLabel.isVisible = messageCount > 0
        }
    }
}