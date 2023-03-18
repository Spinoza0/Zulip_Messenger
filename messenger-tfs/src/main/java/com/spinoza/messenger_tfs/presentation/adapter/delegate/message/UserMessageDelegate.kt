package com.spinoza.messenger_tfs.presentation.adapter.delegate.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.databinding.UserMessageItemBinding
import com.spinoza.messenger_tfs.presentation.adapter.delegate.AdapterDelegate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.bind

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