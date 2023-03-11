package com.spinoza.messenger_tfs.presentation.adapter.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.databinding.UserMessageItemBinding
import com.spinoza.messenger_tfs.presentation.adapter.utils.AdapterDelegate
import com.spinoza.messenger_tfs.presentation.adapter.utils.DelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.utils.bind

class UserMessageDelegate : AdapterDelegate {
    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = UserMessageItemBinding.inflate(
            inflater,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        item: DelegateItem,
        position: Int,
    ) {
        (holder as ViewHolder).bind(item as UserMessageDelegateItem)
    }

    override fun isOfViewType(item: DelegateItem): Boolean {
        return item is UserMessageDelegateItem
    }

    class ViewHolder(
        private val binding: UserMessageItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserMessageDelegateItem) {
            binding.messageView.bind(item)
        }
    }
}