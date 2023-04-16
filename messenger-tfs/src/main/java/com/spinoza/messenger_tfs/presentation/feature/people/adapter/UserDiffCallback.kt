package com.spinoza.messenger_tfs.presentation.feature.people.adapter

import androidx.recyclerview.widget.DiffUtil
import com.spinoza.messenger_tfs.domain.model.User

class UserDiffCallback : DiffUtil.ItemCallback<User>() {

    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.userId == newItem.userId
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: User, newItem: User): Any? {
        return if (oldItem.presence == newItem.presence) null else newItem.presence
    }
}