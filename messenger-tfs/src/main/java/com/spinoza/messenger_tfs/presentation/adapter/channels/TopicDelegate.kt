package com.spinoza.messenger_tfs.presentation.adapter.channels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.databinding.TopicItemBinding
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.presentation.adapter.delegate.AdapterDelegate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem

class TopicDelegate(
    private val template: String,
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
            template,
            evenColor,
            oddColor,
            onClickListener
        )
    }

    override fun isOfViewType(item: DelegateAdapterItem): Boolean {
        return item is TopicDelegateItem
    }

    class ViewHolder(private val binding: TopicItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: TopicDelegateItem,
            position: Int,
            template: String,
            evenColor: Int,
            oddColor: Int,
            onClickListener: (MessagesFilter) -> Unit,
        ) {
            val color = if (position % 2 == 0) evenColor else oddColor
            val messagesFilter = (item.content() as MessagesFilter)
            binding.textViewTopic.setBackgroundColor(color)
            binding.textViewTopic.text = String.format(
                template,
                messagesFilter.topic.name,
                messagesFilter.topic.messageCount
            )
            binding.root.setOnClickListener {
                onClickListener(messagesFilter)
            }
        }
    }
}