package com.spinoza.messenger_tfs.presentation.fragment.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentChannelsPageBinding
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.usecase.GetChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetTopicUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetTopicsUseCase
import com.spinoza.messenger_tfs.presentation.adapter.channels.ChannelDelegate
import com.spinoza.messenger_tfs.presentation.adapter.channels.TopicDelegate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.MainDelegateAdapter
import com.spinoza.messenger_tfs.presentation.fragment.showError
import com.spinoza.messenger_tfs.presentation.state.ChannelsPageScreenState
import com.spinoza.messenger_tfs.presentation.state.ChannelsScreenState
import com.spinoza.messenger_tfs.presentation.ui.getThemeColor
import com.spinoza.messenger_tfs.presentation.ui.off
import com.spinoza.messenger_tfs.presentation.ui.on
import com.spinoza.messenger_tfs.presentation.viewmodel.ChannelsFragmentSharedViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.ChannelsPageFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.ChannelsPageFragmentViewModelFactory
import kotlinx.coroutines.launch

class ChannelsPageFragment : Fragment() {

    private var isAllChannels = false

    private var _binding: FragmentChannelsPageBinding? = null
    private val binding: FragmentChannelsPageBinding
        get() = _binding ?: throw RuntimeException("FragmentChannelsPageBinding == null")

    private val viewModel: ChannelsPageFragmentViewModel by viewModels {
        ChannelsPageFragmentViewModelFactory(
            isAllChannels,
            GetTopicsUseCase(MessagesRepositoryImpl.getInstance()),
            GetChannelsUseCase(MessagesRepositoryImpl.getInstance()),
            GetTopicUseCase(MessagesRepositoryImpl.getInstance()),
        )
    }
    private val sharedViewModel: ChannelsFragmentSharedViewModel by activityViewModels()

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
        val delegateAdapter = MainDelegateAdapter()
        delegateAdapter.addDelegate(
            ChannelDelegate(
                getString(R.string.channel_name_template),
                viewModel::onChannelClickListener
            )
        )
        delegateAdapter.addDelegate(
            TopicDelegate(
                requireContext().getThemeColor(R.attr.even_topic_color),
                requireContext().getThemeColor(R.attr.odd_topic_color),
                viewModel::onTopicClickListener
            )
        )
        binding.recyclerViewChannels.adapter = delegateAdapter
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::handleState)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.state.collect(::handleChannelsScreenState)
            }
        }
    }

    private fun channelsListIsEmpty(): Boolean {
        return (binding.recyclerViewChannels.adapter as MainDelegateAdapter).itemCount == NO_ITEMS
    }

    private fun handleState(state: ChannelsPageScreenState) {
        if (state !is ChannelsPageScreenState.Loading) {
            binding.shimmerLarge.off()
            binding.shimmerSmall.off()
        }
        when (state) {
            is ChannelsPageScreenState.Items ->
                (binding.recyclerViewChannels.adapter as MainDelegateAdapter)
                    .submitList(state.value)
            is ChannelsPageScreenState.TopicMessagesCountUpdate ->
                (binding.recyclerViewChannels.adapter as MainDelegateAdapter)
                    .submitList(state.value)
            is ChannelsPageScreenState.Loading -> {
                if (channelsListIsEmpty()) binding.shimmerLarge.on()
                else binding.shimmerSmall.on()
            }
            is ChannelsPageScreenState.Failure -> handleErrors(state)
        }
    }

    private fun handleErrors(error: ChannelsPageScreenState.Failure) {
        when (error) {
            is ChannelsPageScreenState.Failure.LoadingChannels -> showError(
                String.format(
                    getString(R.string.error_loading_channels),
                    error.channelsFilter.name
                )
            )
            is ChannelsPageScreenState.Failure.LoadingChannelTopics -> showError(
                String.format(
                    getString(R.string.error_loading_topics),
                    error.channel.name
                )
            )
        }
    }

    private fun handleChannelsScreenState(state: ChannelsScreenState) {
        when (state) {
            is ChannelsScreenState.Idle -> {}
            is ChannelsScreenState.Filter -> {
                val filterIsAllChannels = state.value.screenPosition % 2 != 0
                if (filterIsAllChannels == isAllChannels) {
                    viewModel.setChannelsFilter(ChannelsFilter(state.value.text, !isAllChannels))
                }
            }
        }
    }

    private fun parseParams() {
        isAllChannels = arguments?.getBoolean(PARAM_IS_ALL_CHANNELS, false) ?: false
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateMessagesCount()
    }

    override fun onPause() {
        super.onPause()
        binding.shimmerLarge.off()
        binding.shimmerSmall.off()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (binding.recyclerViewChannels.adapter as MainDelegateAdapter).clear()
        binding.recyclerViewChannels.adapter = null
        _binding = null
    }

    companion object {

        private const val PARAM_IS_ALL_CHANNELS = "isAllChannels"
        private const val NO_ITEMS = 0

        fun newInstance(isAllChannels: Boolean): ChannelsPageFragment {
            return ChannelsPageFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(PARAM_IS_ALL_CHANNELS, isAllChannels)
                }
            }
        }
    }
}