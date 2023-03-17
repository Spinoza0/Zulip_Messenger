package com.spinoza.messenger_tfs.presentation.adapter.topic

import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.databinding.TopicItemBinding
import com.spinoza.messenger_tfs.domain.model.Topic

class TopicViewHolder(private val binding: TopicItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        channelId: Long,
        topic: Topic,
        color: Int,
        onClickListener: ((Long, String) -> Unit)?,
    ) {
        binding.textViewTopic.text = topic.name
        binding.textViewTopic.setBackgroundColor(color)
        binding.root.setOnClickListener {
            onClickListener?.invoke(channelId, topic.name)
        }
    }
}
