package com.spinoza.messenger_tfs.presentation.adapter.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.spinoza.messenger_tfs.databinding.UserItemBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.presentation.adapter.bind

class UserAdapter : ListAdapter<User, UserViewHolder>(UserDiffCallback()) {

    lateinit var channel: Channel

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = UserItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.binding.bind(getItem(position))
    }
}