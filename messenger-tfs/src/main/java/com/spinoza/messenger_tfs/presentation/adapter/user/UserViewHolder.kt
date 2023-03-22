package com.spinoza.messenger_tfs.presentation.adapter.user

import android.view.View
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
            val visibility = if (user.isActive) View.VISIBLE else View.GONE
            imageViewCircleBorder.visibility = visibility
            imageViewCircle.visibility = visibility
            root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) onClickListener(user.userId)
            }
        }
    }
}