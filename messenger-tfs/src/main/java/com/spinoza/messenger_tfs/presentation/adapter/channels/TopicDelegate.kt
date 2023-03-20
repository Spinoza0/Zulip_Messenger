package com.spinoza.messenger_tfs.presentation.adapter.channels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.databinding.TopicItemBinding
import com.spinoza.messenger_tfs.domain.model.ChannelFilter
import com.spinoza.messenger_tfs.presentation.adapter.delegate.AdapterDelegate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem

class TopicDelegate(private val config: TopicDelegateConfig) : AdapterDelegate {

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
        (holder as ViewHolder).bind(item as TopicDelegateItem, position, config)
    }

    override fun isOfViewType(item: DelegateAdapterItem): Boolean {
        return item is TopicDelegateItem
    }

    class ViewHolder(private val binding: TopicItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TopicDelegateItem, position: Int, config: TopicDelegateConfig) {
            val color = if (position % 2 == 0) config.evenColor else config.oddColor
            val channelFilter = (item.content() as ChannelFilter)
            with(binding) {
                textViewTopic.setBackgroundColor(color)
                textViewTopic.text = channelFilter.topicName
                root.setOnClickListener {
                    config.onClickListener(channelFilter)
                }
            }
        }
    }
}