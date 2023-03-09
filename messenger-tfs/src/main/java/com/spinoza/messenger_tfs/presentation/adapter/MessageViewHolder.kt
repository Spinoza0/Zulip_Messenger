package com.spinoza.messenger_tfs.presentation.adapter

import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.databinding.MessageItemBinding
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageType
import com.spinoza.messenger_tfs.domain.model.User

class MessageViewHolder(val binding: MessageItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(message: Message, user: User) {
        with(binding.messageView) {
            setMessage(message)
            if (message.user.id == user.id) {
                setMessageType(MessageType.USER)
            } else {
                setMessageType(MessageType.COMPANION)
            }
        }
    }
}