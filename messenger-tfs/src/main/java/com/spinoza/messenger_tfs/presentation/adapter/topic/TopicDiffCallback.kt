package com.spinoza.messenger_tfs.presentation.adapter.topic

import androidx.recyclerview.widget.DiffUtil
import com.spinoza.messenger_tfs.domain.model.Topic

class TopicDiffCallback : DiffUtil.ItemCallback<Topic>() {

    override fun areItemsTheSame(oldItem: Topic, newItem: Topic): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Topic, newItem: Topic): Boolean {
        return oldItem == newItem
    }
}