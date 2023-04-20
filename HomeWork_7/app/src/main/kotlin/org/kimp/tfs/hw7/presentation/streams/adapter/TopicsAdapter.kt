package org.kimp.tfs.hw7.presentation.streams.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.kimp.tfs.hw7.data.api.Topic
import org.kimp.tfs.hw7.databinding.ItemTopicBinding
import org.kimp.tfs.hw7.utils.getTertiaryColor
import org.kimp.tfs.hw7.utils.getTertiaryContainerColor
import timber.log.Timber

class TopicsAdapter(
) : ListAdapter<Topic, TopicsAdapter.TopicViewHolder>(TopicDiffCallback()) {

    var stream: String = ""


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TopicViewHolder(
        ItemTopicBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )


    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class TopicViewHolder(
        private val binding: ItemTopicBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(topic: Topic, position: Int) {
            binding.topicNameTextView.text = topic.name

            val context = binding.root.context
            val (b, f) = when (position % 2) {
                1 -> context.getTertiaryColor() to context.getTertiaryContainerColor()
                else -> context.getTertiaryContainerColor() to context.getTertiaryColor()
            }

            binding.topicNameTextView.setBackgroundColor(b)
            binding.topicNameTextView.setTextColor(f)

            binding.topicNameTextView.setOnClickListener {
                Timber.tag(TAG).i("Requested messages page for $stream:${topic.name}")
                //messagesFragmentRequestListener.onMessagesFragmentRequested(stream, topic.name)
            }
        }
    }


    class TopicDiffCallback : DiffUtil.ItemCallback<Topic>() {
        override fun areItemsTheSame(oldItem: Topic, newItem: Topic) = oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: Topic, newItem: Topic) = oldItem == newItem
    }

    companion object {
        private const val TAG = "TopicsAdapter"
    }
}
