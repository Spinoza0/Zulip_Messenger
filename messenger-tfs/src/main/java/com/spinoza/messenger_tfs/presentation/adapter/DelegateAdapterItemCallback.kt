package com.spinoza.messenger_tfs.presentation.adapter

import androidx.recyclerview.widget.DiffUtil
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateItem

class DelegateAdapterItemCallback : DiffUtil.ItemCallback<DelegateItem>() {

    override fun areItemsTheSame(oldItem: DelegateItem, newItem: DelegateItem): Boolean {
        return oldItem::class == newItem::class && oldItem.id() == newItem.id()
    }

    override fun areContentsTheSame(oldItem: DelegateItem, newItem: DelegateItem): Boolean {
        return oldItem.compareToOther(newItem)
    }
}
