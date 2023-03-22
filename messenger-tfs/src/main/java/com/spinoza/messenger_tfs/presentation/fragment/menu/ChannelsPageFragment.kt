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
import com.spinoza.messenger_tfs.App
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentChannelsPageBinding
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.usecase.GetAllChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetSubscribedChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetTopicsUseCase
import com.spinoza.messenger_tfs.presentation.adapter.channels.ChannelDelegate
import com.spinoza.messenger_tfs.presentation.adapter.channels.TopicDelegate
import com.spinoza.messenger_tfs.presentation.adapter.channels.TopicDelegateConfig
import com.spinoza.messenger_tfs.presentation.adapter.delegate.MainDelegateAdapter
import com.spinoza.messenger_tfs.presentation.model.ChannelItem
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import com.spinoza.messenger_tfs.presentation.state.ChannelsScreenState
import com.spinoza.messenger_tfs.presentation.ui.getThemeColor
import com.spinoza.messenger_tfs.presentation.ui.off
import com.spinoza.messenger_tfs.presentation.ui.on
import com.spinoza.messenger_tfs.presentation.viewmodel.ChannelsFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.ChannelsFragmentViewModelFactory
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChannelsPageFragment : Fragment() {

    private var isAllChannels = false

    private var _binding: FragmentChannelsPageBinding? = null
    private val binding: FragmentChannelsPageBinding
        get() = _binding ?: throw RuntimeException("FragmentChannelsPageBinding == null")

    private val viewModel: ChannelsFragmentViewModel by viewModels {
        ChannelsFragmentViewModelFactory(
            GetTopicsUseCase(MessagesRepositoryImpl.getInstance()),
            GetSubscribedChannelsUseCase(MessagesRepositoryImpl.getInstance()),
            GetAllChannelsUseCase(MessagesRepositoryImpl.getInstance())
        )
    }


    private val delegatesAdapter by lazy {
        MainDelegateAdapter().apply {
            val topicConfig = TopicDelegateConfig(
                requireContext().getString(R.string.channels_topic_template),
                requireContext().getThemeColor(R.attr.even_topic_color),
                requireContext().getThemeColor(R.attr.odd_topic_color),
                ::onTopicClickListener
            )
            addDelegate(ChannelDelegate(::onChannelClickListener))
            addDelegate(TopicDelegate(topicConfig))
        }
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

        setupRecyclerView()
        parseParams()
        setupObservers()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewChannels.adapter = delegatesAdapter
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                getState(isAllChannels).collect(::handleState)
            }
        }
    }

    private fun handleState(state: ChannelsScreenState) {
        if (state !is ChannelsScreenState.Loading) {
            binding.progressBar.off()
        }
        when (state) {
            is ChannelsScreenState.Items -> delegatesAdapter.submitList(state.value)
            is ChannelsScreenState.Loading -> binding.progressBar.on()
            // TODO: show errors
            is ChannelsScreenState.Error -> {}
        }
    }

    private fun getState(isAllChannels: Boolean): StateFlow<ChannelsScreenState> {
        return if (isAllChannels) viewModel.stateAllItems else viewModel.stateSubscribedItems
    }

    private fun onChannelClickListener(channelItem: ChannelItem) {
        viewModel.onChannelClickListener(channelItem)
    }

    private fun onTopicClickListener(messagesFilter: MessagesFilter) {
        App.router.navigateTo(Screens.Messages(messagesFilter))
    }

    private fun parseParams() {
        isAllChannels = arguments?.getBoolean(PARAM_IS_ALL_CHANNELS, false) ?: false
    }

    override fun onResume() {
        super.onResume()

        if (binding.recyclerViewChannels.adapter?.itemCount == NO_ITEMS) {
            viewModel.loadItems(isAllChannels)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
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