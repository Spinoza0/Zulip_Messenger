package com.spinoza.messenger_tfs.presentation.adapter.message

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.databinding.CompanionMessageItemBinding
import com.spinoza.messenger_tfs.presentation.adapter.utils.AdapterDelegate
import com.spinoza.messenger_tfs.presentation.adapter.utils.DelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.utils.bind

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