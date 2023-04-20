package com.spinoza.messenger_tfs.presentation.feature.people.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.UserItemBinding
import com.spinoza.messenger_tfs.domain.model.User

class UserViewHolder(private val binding: UserItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: User, onClickListener: (Long) -> Unit) {
        with(binding) {
            textViewName.text = user.fullName
            textViewEmail.text = user.email
            Glide.with(imageViewAvatar)
                .load(user.avatarUrl)
                .circleCrop()
                .error(R.drawable.ic_default_avatar)
                .into(imageViewAvatar)
            root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) onClickListener(user.userId)
            }
            bind(user.presence)
        }
    }

    fun bind(presence: User.Presence) {
        binding.imageViewCircleOnline.isVisible = presence == User.Presence.ACTIVE
        binding.imageViewCircleIdle.isVisible = presence == User.Presence.IDLE
        binding.imageViewCircleBorder.isVisible = binding.imageViewCircleOnline.isVisible ||
                binding.imageViewCircleIdle.isVisible
    }
}