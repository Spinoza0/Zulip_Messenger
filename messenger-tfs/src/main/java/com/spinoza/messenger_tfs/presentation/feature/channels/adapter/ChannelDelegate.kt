package com.spinoza.messenger_tfs.presentation.feature.channels.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.ChannelItemBinding
import com.spinoza.messenger_tfs.presentation.adapter.AdapterDelegate
import com.spinoza.messenger_tfs.presentation.adapter.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.feature.channels.model.ChannelItem

class ChannelDelegate(
    private val channel_name_template: String,
    private val onChannelClickListener: (ChannelItem) -> Unit,
    private val onChannelLongClickListener: (ChannelItem) -> Unit,
    private val onArrowClickListener: (ChannelItem) -> Unit,
) : AdapterDelegate {

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
        (holder as ViewHolder).bind(
            channel_name_template,
            item as ChannelDelegateItem,
            onChannelClickListener,
            onChannelLongClickListener,
            onArrowClickListener
        )
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

        fun bind(
            channel_name_template: String,
            item: ChannelDelegateItem,
            onChannelClickListener: (ChannelItem) -> Unit,
            onChannelLongClickListener: (ChannelItem) -> Unit,
            onArrowClickListener: (ChannelItem) -> Unit,
        ) {
            val channelItem = (item.content() as ChannelItem)
            with(binding) {
                textViewChannel.text =
                    String.format(channel_name_template, channelItem.channel.name)
                frameLayoutChannel.setOnClickListener {
                    onChannelClickListener.invoke(channelItem)
                }
                textViewChannel.setOnClickListener {
                    onChannelClickListener.invoke(channelItem)
                }
                textViewArrowArea.setOnClickListener {
                    onArrowClickListener.invoke(channelItem)
                }
                frameLayoutChannel.setOnLongClickListener {
                    onChannelLongClickListener.invoke(channelItem)
                    true
                }
                textViewChannel.setOnLongClickListener {
                    onChannelLongClickListener.invoke(channelItem)
                    true
                }
                textViewArrowArea.setOnLongClickListener {
                    onChannelLongClickListener.invoke(channelItem)
                    true
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