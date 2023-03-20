package com.spinoza.messenger_tfs.presentation.adapter.topic

import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.databinding.TopicItemBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.Topic

class TopicViewHolder(private val binding: TopicItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        channel: Channel,
        topic: Topic,
        position: Int,
        config: TopicAdapterConfig,
    ) {
        val color = if (position % 2 == 0) config.evenColor else config.oddColor
        with(binding) {
            textViewTopic.text = String.format(config.template, topic.name, topic.messageCount)
            textViewTopic.setBackgroundColor(color)
            root.setOnClickListener {
                config.onClickListener.invoke(channel, topic.name)
            }
        }
    }
}