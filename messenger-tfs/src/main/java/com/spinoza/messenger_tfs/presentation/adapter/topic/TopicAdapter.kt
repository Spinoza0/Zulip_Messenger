package com.spinoza.messenger_tfs.presentation.adapter.topic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.spinoza.messenger_tfs.databinding.TopicItemBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.Topic

class TopicAdapter(
    private val evenColor: Int,
    private val oddColor: Int,
) : ListAdapter<Topic, TopicViewHolder>(TopicDiffCallback()) {

    lateinit var channel: Channel
    var onClickListener: ((Channel, String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val binding = TopicItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TopicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val color = if (position % 2 == 0) evenColor else oddColor
        holder.bind(channel, getItem(position), color, onClickListener)
    }
}