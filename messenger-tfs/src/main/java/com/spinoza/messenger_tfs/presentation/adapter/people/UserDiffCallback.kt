package com.spinoza.messenger_tfs.presentation.adapter.people

import androidx.recyclerview.widget.DiffUtil
import com.spinoza.messenger_tfs.domain.model.User

class UserDiffCallback : DiffUtil.ItemCallback<User>() {

    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.userId == newItem.userId
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}