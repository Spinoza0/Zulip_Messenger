package com.spinoza.messenger_tfs.presentation.adapter.delegate.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.databinding.CompanionMessageItemBinding
import com.spinoza.messenger_tfs.presentation.adapter.bind
import com.spinoza.messenger_tfs.presentation.adapter.delegate.AdapterDelegate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateItem

class CompanionMessageDelegate : AdapterDelegate {

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CompanionMessageItemBinding.inflate(
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
        (holder as ViewHolder).binding.messageView.bind(item as CompanionMessageDelegateItem)
    }

    override fun isOfViewType(item: DelegateItem): Boolean {
        return item is CompanionMessageDelegateItem
    }

    class ViewHolder(val binding: CompanionMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}