package com.spinoza.messenger_tfs.presentation.adapter.message

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

interface AdapterDelegate {

    fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder

    fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: DelegateAdapterItem, position: Int)

    fun isOfViewType(item: DelegateAdapterItem): Boolean
}