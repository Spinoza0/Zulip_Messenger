package com.spinoza.messenger_tfs.presentation.fragment.menu

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.spinoza.messenger_tfs.MessengerApp
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.FragmentChannelsPageBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.presentation.adapter.channel.ChannelsAdapter
import com.spinoza.messenger_tfs.presentation.adapter.topic.TopicAdapter
import com.spinoza.messenger_tfs.presentation.cicerone.Screens
import com.spinoza.messenger_tfs.presentation.model.ChannelsFragmentState
import com.spinoza.messenger_tfs.presentation.ui.getThemeColor
import kotlinx.coroutines.launch

class ChannelsPageFragment : Fragment() {

    lateinit var adapter: ChannelsAdapter

    private lateinit var channelsViewModel: ChannelsViewModel
    private var allChannels = false

    private var _binding: FragmentChannelsPageBinding? = null
    private val binding: FragmentChannelsPageBinding
        get() = _binding ?: throw RuntimeException("FragmentChannelsPageBinding == null")

    override fun onAttach(context: Context) {
        super.onAttach(context)
        channelsViewModel = parentFragment as ChannelsViewModel
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

        parseParams()
        setupRecyclerView()
        setupObservers()
        channelsViewModel.getChannels(allChannels)
    }

    private fun setupRecyclerView() {
        adapter = ChannelsAdapter(
            allChannels,
            requireContext().getThemeColor(R.attr.even_topic_color),
            requireContext().getThemeColor(R.attr.odd_topic_color),
            channelsViewModel::onChannelClickListener,
            ::onTopicClickListener
        )
        binding.recyclerViewChannels.adapter = adapter
    }

    private fun onTopicClickListener(channel: Channel, topicName: String) {
        MessengerApp.router.navigateTo(Screens.Messages(channel, topicName))
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                channelsViewModel.getState(allChannels).collect { state ->
                    when (state) {
                        is ChannelsFragmentState.Channels -> adapter.submitList(state.channels)
                        is ChannelsFragmentState.Topics -> handleTopicsState(state)
                        is ChannelsFragmentState.Idle -> {}

                        // TODO: show errors
                        is ChannelsFragmentState.Error -> {}
                    }
                }
            }
        }
    }

    private fun handleTopicsState(state: ChannelsFragmentState.Topics) {
        when (state.channel.type) {
            Channel.Type.FOLDED -> {
                state.binding.imageViewArrow.setImageResource(R.drawable.ic_arrow_down)
            }
            Channel.Type.UNFOLDED -> {
                state.binding.imageViewArrow.setImageResource(R.drawable.ic_arrow_up)
            }
        }
        (state.binding.recyclerViewTopics.adapter as TopicAdapter).submitList(state.topics)
    }

    private fun parseParams() {
        allChannels = arguments?.getBoolean(EXTRA_ALL_CHANNELS, false) ?: false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        private const val EXTRA_ALL_CHANNELS = "allChannels"

        fun newInstance(allChannels: Boolean): ChannelsPageFragment {
            return ChannelsPageFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(EXTRA_ALL_CHANNELS, allChannels)
                }
            }
        }
    }
}