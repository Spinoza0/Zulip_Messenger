package org.kimp.tfs.hw7.presentation.streams.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.kimp.tfs.hw7.R
import org.kimp.tfs.hw7.data.api.Stream
import org.kimp.tfs.hw7.data.api.Topic
import org.kimp.tfs.hw7.databinding.ItemStreamBinding

class StreamsAdapter(
) : ListAdapter<Pair<Stream, List<Topic>>, StreamsAdapter.StreamViewHolder>(StreamDiffCallback()) {
    private val expandedStreams = mutableSetOf<Int>()

    override fun onBindViewHolder(holder: StreamViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        StreamViewHolder(ItemStreamBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ).also { it.topicsRecyclerView.adapter = TopicsAdapter(/*messagesFragmentRequestListener*/) })


    inner class StreamViewHolder(
        private val binding: ItemStreamBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Pair<Stream, List<Topic>>) {
            val (stream, topics) = data
            binding.streamNameTextView.text = stream.name

            if (expandedStreams.contains(stream.id)) {
                binding.expandButton.setIconResource(R.drawable.ic_arrow_drop_up)
            } else {
                binding.expandButton.setIconResource(R.drawable.ic_arrow_drop_down)
            }

            (binding.topicsRecyclerView.adapter as TopicsAdapter).also {
                it.submitList(topics)
                it.stream = stream.name
            }
        }
    }


    class StreamDiffCallback : DiffUtil.ItemCallback<Pair<Stream, List<Topic>>>() {
        override fun areItemsTheSame(
            oldItem: Pair<Stream, List<Topic>>, newItem: Pair<Stream, List<Topic>>
        ) = oldItem.first.id == newItem.first.id

        override fun areContentsTheSame(
            oldItem: Pair<Stream, List<Topic>>, newItem: Pair<Stream, List<Topic>>
        ): Boolean {
            return oldItem.first == newItem.first
            // TODO add second comparing
        }
    }
}
