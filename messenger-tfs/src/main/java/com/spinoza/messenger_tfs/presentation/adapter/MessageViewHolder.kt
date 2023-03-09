package com.spinoza.messenger_tfs.presentation.adapter

import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.databinding.MessageItemBinding
import com.spinoza.messenger_tfs.domain.model.Message

class MessageViewHolder(val binding: MessageItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(message: Message) {
        binding.messageView.setMessage(message)
    }
}