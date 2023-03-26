package com.spinoza.messenger_tfs.presentation.adapter.channels

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.ChannelItemBinding
import com.spinoza.messenger_tfs.presentation.adapter.delegate.AdapterDelegate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.model.ChannelItem

class ChannelDelegate(private val onChannelClickListener: (ChannelItem) -> Unit) : AdapterDelegate {

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ChannelItemBinding.inflate(
            inflater,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        item: DelegateAdapterItem,
        position: Int,
    ) {
        (holder as ViewHolder).bind(item as ChannelDelegateItem, onChannelClickListener)
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
        return item is ChannelDelegateItem
    }

    class ViewHolder(private val binding: ChannelItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChannelDelegateItem, onChannelClickListener: (ChannelItem) -> Unit) {
            val channelItem = (item.content() as ChannelItem)
            with(binding) {
                textViewChannel.text = channelItem.channel.name
                root.setOnClickListener {
                    onChannelClickListener.invoke(channelItem)
                }

                if (channelItem.isFolded) {
                    imageViewArrow.setImageResource(R.drawable.ic_arrow_down)
                    textViewChannel.setTypeface(null, Typeface.NORMAL)
                } else {
                    imageViewArrow.setImageResource(R.drawable.ic_arrow_up)
                    textViewChannel.setTypeface(null, Typeface.BOLD)
                }
            }
        }
    }
}