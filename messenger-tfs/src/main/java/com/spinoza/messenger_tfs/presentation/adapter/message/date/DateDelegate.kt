package com.spinoza.messenger_tfs.presentation.adapter.message.date

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.databinding.DateItemBinding
import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.AdapterDelegate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem

class DateDelegate : AdapterDelegate {

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return ViewHolder(
            DateItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        item: DelegateAdapterItem,
        position: Int,
    ) {
        (holder as ViewHolder).bind(item.content() as MessageDate)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        item: DelegateAdapterItem,
        position: Int,
        payloads: List<Any>,
    ) {
        onBindViewHolder(holder, item, position)
    }

    override fun isOfViewType(item: DelegateAdapterItem): Boolean {
        return item is DateDelegateItem
    }

    class ViewHolder(private val binding: DateItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(model: MessageDate) {
            binding.textViewDate.text = model.date
        }
    }
}