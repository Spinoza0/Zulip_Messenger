package com.spinoza.messenger_tfs.presentation.fragment.menu

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.spinoza.messenger_tfs.App
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.FragmentChannelsPageBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.presentation.adapter.channel.ChannelsAdapter
import com.spinoza.messenger_tfs.presentation.adapter.topic.TopicAdapter
import com.spinoza.messenger_tfs.presentation.adapter.topic.TopicAdapterConfig
import com.spinoza.messenger_tfs.presentation.model.ChannelItem
import com.spinoza.messenger_tfs.presentation.model.ChannelsFragmentState
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import com.spinoza.messenger_tfs.presentation.ui.getThemeColor
import com.spinoza.messenger_tfs.presentation.ui.off
import com.spinoza.messenger_tfs.presentation.ui.on
import kotlinx.coroutines.launch

class ChannelsPageFragment : Fragment() {

    private lateinit var adapter: ChannelsAdapter
    private lateinit var channelsPageParent: ChannelsPageParent
    private var isAllChannels = false

    private var _binding: FragmentChannelsPageBinding? = null
    private val binding: FragmentChannelsPageBinding
        get() = _binding ?: throw RuntimeException("FragmentChannelsPageBinding == null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChannelsPageBinding.inflate(inflater, container, false)
        channelsPageParent = parentFragment as ChannelsPageParent

        setupRecyclerView()
        parseParams()
        setupObservers()
        channelsPageParent.loadChannels(isAllChannels)

        return binding.root
    }

    private fun setupRecyclerView() {
        val topicConfig = TopicAdapterConfig(
            requireContext().getString(R.string.channels_topic_template),
            requireContext().getThemeColor(R.attr.even_topic_color),
            requireContext().getThemeColor(R.attr.odd_topic_color),
            ::onTopicClickListener
        )
        adapter =
            ChannelsAdapter(isAllChannels, topicConfig, channelsPageParent::onChannelClickListener)
        binding.recyclerViewChannels.adapter = adapter
    }

    private fun onTopicClickListener(channel: Channel, topicName: String) {
        App.router.navigateTo(Screens.Messages(channel, topicName))
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                channelsPageParent.getState(isAllChannels).collect(::handleChannelsFragmentState)
            }
        }
    }

    private fun handleChannelsFragmentState(state: ChannelsFragmentState) {
        if (state !is ChannelsFragmentState.Loading) {
            binding.progressBar.off()
        }
        when (state) {
            is ChannelsFragmentState.Channels -> adapter.submitList(state.channels)
            is ChannelsFragmentState.Topics -> handleTopicsState(state)
            is ChannelsFragmentState.Loading -> binding.progressBar.on()
            // TODO: show errors
            is ChannelsFragmentState.Error -> {}
        }
    }

    private fun handleTopicsState(state: ChannelsFragmentState.Topics) {
        when (state.channel.type) {
            ChannelItem.Type.FOLDED -> {
                state.binding.imageViewArrow.setImageResource(R.drawable.ic_arrow_down)
                state.binding.textViewChannel.setTypeface(null, Typeface.NORMAL)
            }
            ChannelItem.Type.UNFOLDED -> {
                state.binding.imageViewArrow.setImageResource(R.drawable.ic_arrow_up)
                state.binding.textViewChannel.setTypeface(null, Typeface.BOLD)
            }
        }
        (state.binding.recyclerViewTopics.adapter as? TopicAdapter)?.submitList(state.topics)
    }

    private fun parseParams() {
        isAllChannels = arguments?.getBoolean(EXTRA_IS_ALL_CHANNELS, false) ?: false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        private const val EXTRA_IS_ALL_CHANNELS = "isAllChannels"

        fun newInstance(isAllChannels: Boolean): ChannelsPageFragment {
            return ChannelsPageFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(EXTRA_IS_ALL_CHANNELS, isAllChannels)
                }
            }
        }
    }
}