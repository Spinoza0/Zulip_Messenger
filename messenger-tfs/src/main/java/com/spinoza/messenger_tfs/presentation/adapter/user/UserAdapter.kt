package com.spinoza.messenger_tfs.presentation.adapter.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.spinoza.messenger_tfs.databinding.UserItemBinding
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.presentation.adapter.bind

class UserAdapter(
    private val onClickListener: (Long) -> Unit,
) : ListAdapter<User, UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = UserItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.binding.bind(getItem(position), onClickListener)
    }
}