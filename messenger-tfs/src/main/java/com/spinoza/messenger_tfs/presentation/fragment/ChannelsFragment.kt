package com.spinoza.messenger_tfs.presentation.fragment

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
import com.spinoza.messenger_tfs.databinding.FragmentChannelsBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.usecase.GetAllChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetSubscribedChannelsUseCase
import com.spinoza.messenger_tfs.presentation.adapter.channel.ChannelsAdapter
import com.spinoza.messenger_tfs.presentation.adapter.topic.TopicAdapter
import com.spinoza.messenger_tfs.presentation.cicerone.Screens
import com.spinoza.messenger_tfs.presentation.model.ChannelsFragmentState
import com.spinoza.messenger_tfs.presentation.model.ChannelsFragmentState.SourceType
import com.spinoza.messenger_tfs.presentation.ui.getThemeColor
import com.spinoza.messenger_tfs.presentation.viewmodel.ChannelsFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.ChannelsFragmentViewModelFactory
import kotlinx.coroutines.launch

class ChannelsFragment : Fragment() {

    private var _binding: FragmentChannelsBinding? = null
    private val binding: FragmentChannelsBinding
        get() = _binding ?: throw RuntimeException("FragmentChannelsBinding == null")

    private val adapter by lazy {
        ChannelsAdapter(
            requireContext().getThemeColor(R.attr.even_topic_color),
            requireContext().getThemeColor(R.attr.odd_topic_color),
            viewModel::onChannelClickListener,
            ::onTopicClickListener
        )
    }

    private val viewModel: ChannelsFragmentViewModel by viewModels {
        ChannelsFragmentViewModelFactory(
            GetAllChannelsUseCase(MessagesRepositoryImpl.getInstance()),
            GetSubscribedChannelsUseCase(MessagesRepositoryImpl.getInstance()),
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupScreen()
    }

    private fun setupScreen() {
        binding.recyclerViewChannels.adapter = adapter

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.textViewSubscribedStreams.setOnClickListener {
            viewModel.switchSource(SourceType.SUBSCRIBED)
        }
        binding.textViewAllStreams.setOnClickListener {
            viewModel.switchSource(SourceType.ALL)
        }
    }

    private fun onTopicClickListener(channelId: Long, topicName: String) {
        MessengerApp.router.navigateTo(Screens.Messages(channelId, topicName))
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is ChannelsFragmentState.Source -> handleSourceState(state.type)
                        is ChannelsFragmentState.Channels -> adapter.submitList(state.channels)
                        is ChannelsFragmentState.Topics -> handleTopicsState(state)

                        // TODO: show errors
                        is ChannelsFragmentState.Error -> {}
                    }
                }
            }
        }
    }

    private fun handleSourceState(type: SourceType) {
        when (type) {
            SourceType.SUBSCRIBED -> {
                binding.textViewSubscribedUnderline.visibility = View.VISIBLE
                binding.textViewAllUnderline.visibility = View.INVISIBLE
            }
            SourceType.ALL -> {
                binding.textViewSubscribedUnderline.visibility = View.INVISIBLE
                binding.textViewAllUnderline.visibility = View.VISIBLE
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): ChannelsFragment {
            return ChannelsFragment()
        }
    }
}