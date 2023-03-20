package com.spinoza.messenger_tfs.presentation.adapter.message.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.databinding.UserMessageItemBinding
import com.spinoza.messenger_tfs.presentation.adapter.message.AdapterDelegate
import com.spinoza.messenger_tfs.presentation.adapter.message.DelegateItem

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
        (holder as ViewHolder).binding.messageView.bind(item as UserMessageDelegateItem)
    }

    override fun isOfViewType(item: DelegateItem): Boolean {
        return item is UserMessageDelegateItem
    }

    class ViewHolder(val binding: UserMessageItemBinding) : RecyclerView.ViewHolder(binding.root)
}