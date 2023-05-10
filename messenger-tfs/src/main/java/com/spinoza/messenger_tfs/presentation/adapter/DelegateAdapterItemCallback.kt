package com.spinoza.messenger_tfs.presentation.adapter

import androidx.recyclerview.widget.DiffUtil

class DelegateAdapterItemCallback : DiffUtil.ItemCallback<DelegateAdapterItem>() {

    override fun areItemsTheSame(
        oldItem: DelegateAdapterItem,
        newItem: DelegateAdapterItem,
    ): Boolean {
        return oldItem::class == newItem::class && oldItem.id() == newItem.id()
    }

    override fun areContentsTheSame(
        oldItem: DelegateAdapterItem,
        newItem: DelegateAdapterItem,
    ): Boolean {
        return oldItem::class == newItem::class && oldItem.compareToOther(newItem)
    }

    override fun getChangePayload(
        oldItem: DelegateAdapterItem,
        newItem: DelegateAdapterItem,
    ): Any? {
        return if (oldItem::class == newItem::class) oldItem.getChangePayload(newItem) else null
    }
}