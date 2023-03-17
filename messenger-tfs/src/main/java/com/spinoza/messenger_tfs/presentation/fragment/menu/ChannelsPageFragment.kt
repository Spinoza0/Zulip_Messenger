package com.spinoza.messenger_tfs.presentation.fragment.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.spinoza.messenger_tfs.MessengerApp
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentChannelsPageBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.usecase.GetAllChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetSubscribedChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetTopicsUseCase
import com.spinoza.messenger_tfs.presentation.adapter.channel.ChannelsAdapter
import com.spinoza.messenger_tfs.presentation.adapter.topic.TopicAdapter
import com.spinoza.messenger_tfs.presentation.cicerone.Screens
import com.spinoza.messenger_tfs.presentation.model.ChannelsFragmentState
import com.spinoza.messenger_tfs.presentation.ui.getThemeColor
import com.spinoza.messenger_tfs.presentation.viewmodel.ChannelsFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.ChannelsFragmentViewModelFactory
import kotlinx.coroutines.launch

class ChannelsPageFragment : Fragment() {

    private var _binding: FragmentChannelsPageBinding? = null
    private val binding: FragmentChannelsPageBinding
        get() = _binding ?: throw RuntimeException("FragmentChannelsPageBinding == null")

    private var allChannels = false

    private val adapter by lazy {
        ChannelsAdapter(
            requireContext().getThemeColor(R.attr.even_topic_color),
            requireContext().getThemeColor(R.attr.odd_topic_color),
            viewModel::onChannelClickListener,
            ::onTopicClickListener
        )
    }

    private val viewModel: ChannelsFragmentViewModel by viewModels {
        val getChannelsUseCase = if (allChannels) {
            GetAllChannelsUseCase(MessagesRepositoryImpl.getInstance())
        } else {
            GetSubscribedChannelsUseCase(MessagesRepositoryImpl.getInstance())
        }
        ChannelsFragmentViewModelFactory(
            getChannelsUseCase,
            GetTopicsUseCase(MessagesRepositoryImpl.getInstance())
        )
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
    }

    private fun setupRecyclerView() {
        binding.recyclerViewChannels.adapter = adapter
    }

    private fun onTopicClickListener(channelId: Long, topicName: String) {
        MessengerApp.router.navigateTo(Screens.Messages(channelId, topicName))
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.state.collect { state ->
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