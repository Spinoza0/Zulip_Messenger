package org.kimp.tfs.hw7.presentation.people.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.request.ImageRequest
import org.kimp.tfs.hw7.data.api.Profile
import org.kimp.tfs.hw7.databinding.ItemUserBinding

class UsersAdapter(
    private val imageLoader: ImageLoader
): ListAdapter<Profile, UsersAdapter.UserItemViewHolder>(UserDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = UserItemViewHolder(
        ItemUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: UserItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserItemViewHolder(
        private val binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: Profile) {
            ImageRequest.Builder(binding.baseLayout.context).data(user.avatarUrl)
                .allowHardware(false).target(onSuccess = { binding.avatarView.setAvatarSource(it) })
                .build().also { imageLoader.enqueue(it) }

            binding.user = user
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<Profile>() {
        override fun areContentsTheSame(oldItem: Profile, newItem: Profile) = oldItem == newItem
        override fun areItemsTheSame(oldItem: Profile, newItem: Profile) = oldItem.id == newItem.id
    }
}