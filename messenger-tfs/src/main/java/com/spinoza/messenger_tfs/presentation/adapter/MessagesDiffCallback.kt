package com.spinoza.messenger_tfs.presentation.adapter

import androidx.recyclerview.widget.DiffUtil
import com.spinoza.messenger_tfs.domain.model.Message

class MessagesDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }
}

