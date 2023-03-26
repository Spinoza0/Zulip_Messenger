package com.spinoza.messenger_tfs.presentation.adapter.delegate

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

interface AdapterDelegate {

    fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder

    fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: DelegateAdapterItem, position: Int)

    fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        item: DelegateAdapterItem,
        position: Int,
        payloads: List<Any>,
    )

    fun isOfViewType(item: DelegateAdapterItem): Boolean
}