package com.spinoza.messenger_tfs.presentation.adapter.utils

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

interface AdapterDelegate {

    fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder

    fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: DelegateItem, position: Int)

    fun isOfViewType(item: DelegateItem): Boolean
}