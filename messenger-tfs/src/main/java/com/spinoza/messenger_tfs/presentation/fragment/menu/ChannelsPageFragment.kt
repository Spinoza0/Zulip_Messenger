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
import com.spinoza.messenger_tfs.domain.model.ChannelFilter
import com.spinoza.messenger_tfs.presentation.adapter.channel.ChannelsAdapter
import com.spinoza.messenger_tfs.presentation.adapter.topic.TopicAdapter
import com.spinoza.messenger_tfs.presentation.adapter.topic.TopicAdapterConfig
import com.spinoza.messenger_tfs.presentation.model.ChannelItem
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import com.spinoza.messenger_tfs.presentation.state.ChannelsScreenState
import com.spinoza.messenger_tfs.presentation.ui.getThemeColor
import com.spinoza.messenger_tfs.presentation.ui.off
import com.spinoza.messenger_tfs.presentation.ui.on
import kotlinx.coroutines.launch

class ChannelsPageFragment : Fragment() {

    private lateinit var channelsPageParent: ChannelsPageParent
    private var isAllChannels = false

    private var _binding: FragmentChannelsPageBinding? = null
    private val binding: FragmentChannelsPageBinding
        get() = _binding ?: throw RuntimeException("FragmentChannelsPageBinding == null")

    private val adapter by lazy {
        val topicConfig = TopicAdapterConfig(
            requireContext().getString(R.string.channels_topic_template),
            requireContext().getThemeColor(R.attr.even_topic_color),
            requireContext().getThemeColor(R.attr.odd_topic_color),
            ::onTopicClickListener
        )
        ChannelsAdapter(isAllChannels, topicConfig, channelsPageParent::onChannelClickListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChannelsPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        channelsPageParent = parentFragment as ChannelsPageParent
        setupRecyclerView()
        parseParams()
        setupObservers()

        if (savedInstanceState == null) {
            channelsPageParent.loadChannels(isAllChannels)
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewChannels.adapter = adapter
    }

    private fun onTopicClickListener(channel: Channel, topicName: String) {
        App.router.navigateTo(Screens.Messages(ChannelFilter(channel, topicName)))
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                channelsPageParent.getState(isAllChannels).collect(::handleChannelsFragmentState)
            }
        }
    }

    private fun handleChannelsFragmentState(state: ChannelsScreenState) {
        if (state !is ChannelsScreenState.Loading) {
            binding.progressBar.off()
        }
        when (state) {
            is ChannelsScreenState.Channels -> adapter.submitList(state.channels)
            is ChannelsScreenState.Topics -> handleTopicsState(state)
            is ChannelsScreenState.Loading -> binding.progressBar.on()
            // TODO: show errors
            is ChannelsScreenState.Error -> {}
        }
    }

    private fun handleTopicsState(state: ChannelsScreenState.Topics) {
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
        isAllChannels = arguments?.getBoolean(PARAM_IS_ALL_CHANNELS, false) ?: false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        private const val PARAM_IS_ALL_CHANNELS = "isAllChannels"

        fun newInstance(isAllChannels: Boolean): ChannelsPageFragment {
            return ChannelsPageFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(PARAM_IS_ALL_CHANNELS, isAllChannels)
                }
            }
        }
    }
}