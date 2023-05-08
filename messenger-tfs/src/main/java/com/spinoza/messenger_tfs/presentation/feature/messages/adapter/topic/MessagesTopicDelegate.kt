package com.spinoza.messenger_tfs.presentation.feature.messages.adapter.topic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.databinding.MessagesTopicItemBinding
import com.spinoza.messenger_tfs.presentation.adapter.AdapterDelegate
import com.spinoza.messenger_tfs.presentation.adapter.DelegateAdapterItem

class MessagesTopicDelegate(
    private val topicNameTemplate: String,
    private val onClickListener: (String) -> Unit,
) :
    AdapterDelegate {

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = MessagesTopicItemBinding.inflate(
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
            item as MessagesTopicDelegateItem,
            topicNameTemplate,
            onClickListener
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
        return item is MessagesTopicDelegateItem
    }

    class ViewHolder(private val binding: MessagesTopicItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MessagesTopicDelegateItem,
            topicNameTemplate: String,
            onClickListener: (String) -> Unit,
        ) {
            val topicName = (item.content() as String)
            binding.textViewTopicName.text = String.format(topicNameTemplate, topicName)
            binding.root.setOnClickListener {
                onClickListener(topicName)
            }
        }
    }
}