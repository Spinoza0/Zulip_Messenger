package com.spinoza.messenger_tfs.presentation.adapter.people

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.UserItemBinding
import com.spinoza.messenger_tfs.domain.model.User

class UserViewHolder(private val binding: UserItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: User, onClickListener: (Long) -> Unit) {
        with(binding) {
            textViewName.text = user.full_name
            textViewEmail.text = user.email
            Glide.with(imageViewAvatar)
                .load(user.avatar_url)
                .circleCrop()
                .error(R.drawable.ic_default_avatar)
                .into(imageViewAvatar)
            imageViewCircleOnline.isVisible = user.presence == User.Presence.ONLINE
            imageViewCircleIdle.isVisible = user.presence == User.Presence.IDLE
            imageViewCircleOffline.isVisible = user.presence == User.Presence.OFFLINE
            root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) onClickListener(user.userId)
            }
        }
    }
}