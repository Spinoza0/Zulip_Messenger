package com.spinoza.messenger_tfs.presentation.adapter.message

import androidx.recyclerview.widget.DiffUtil

class DelegateAdapterItemCallback : DiffUtil.ItemCallback<DelegateAdapterItem>() {

    override fun areItemsTheSame(oldItem: DelegateAdapterItem, newItem: DelegateAdapterItem): Boolean {
        return oldItem::class == newItem::class && oldItem.id() == newItem.id()
    }

    override fun areContentsTheSame(oldItem: DelegateAdapterItem, newItem: DelegateAdapterItem): Boolean {
        return oldItem.compareToOther(newItem)
    }
}
