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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.FragmentChannelsPageBinding
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.presentation.adapter.channels.ChannelDelegate
import com.spinoza.messenger_tfs.presentation.adapter.channels.TopicDelegate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.MainDelegateAdapter
import com.spinoza.messenger_tfs.presentation.fragment.closeApplication
import com.spinoza.messenger_tfs.presentation.fragment.showCheckInternetConnectionDialog
import com.spinoza.messenger_tfs.presentation.fragment.showError
import com.spinoza.messenger_tfs.presentation.model.channels.*
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

    private val store: ChannelsPageFragmentViewModel by viewModels {
        ChannelsPageFragmentViewModelFactory(isAllChannels)
    }
    private val sharedStore: ChannelsFragmentSharedViewModel by activityViewModels()

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
            ChannelDelegate(getString(R.string.channel_name_template), ::onChannelClickListener)
        )
        delegateAdapter.addDelegate(
            TopicDelegate(
                requireContext().getThemeColor(R.attr.even_topic_color),
                requireContext().getThemeColor(R.attr.odd_topic_color),
                ::onTopicClickListener
            )
        )
        binding.recyclerViewChannels.adapter = delegateAdapter
        binding.recyclerViewChannels.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val lastItem = layoutManager.itemCount - 1
                val firstItem = 0
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    if (lastVisibleItemPosition == lastItem ||
                        firstVisibleItemPosition == firstItem
                    ) {
                        updateChannelsList()
                    }
                }
            }
        })
    }

    private fun onChannelClickListener(channelItem: ChannelItem) {
        store.accept(ChannelsPageScreenEvent.Ui.OnChannelClick(channelItem))
    }

    private fun onTopicClickListener(messagesFilter: MessagesFilter) {
        store.accept(ChannelsPageScreenEvent.Ui.OnTopicClick(messagesFilter))
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                store.state.collect(::handleState)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedStore.state.collect(::handleSharedScreenState)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                store.effects.collect(::handleEffect)
            }
        }
    }

    private fun handleState(state: ChannelsPageScreenState) {
        if (state.isLoading) {
            binding.shimmerLarge.on()
        } else {
            binding.shimmerLarge.off()
        }
        state.items?.let {
            (binding.recyclerViewChannels.adapter as MainDelegateAdapter).submitList(it)
        }
    }

    private fun handleEffect(effect: ChannelsPageScreenEffect) {
        when (effect) {
            is ChannelsPageScreenEffect.Failure.Error -> showError(
                String.format(getString(R.string.error_channels), effect.value)
            )
            is ChannelsPageScreenEffect.Failure.Network ->
                showCheckInternetConnectionDialog(
                    { store.accept(ChannelsPageScreenEvent.Ui.Load) }
                ) {
                    closeApplication()
                }
        }
    }

    private fun handleSharedScreenState(state: ChannelsScreenState) {
        state.filter?.let { filter ->
            val filterIsAllChannels = filter.screenPosition % 2 != 0
            if (filterIsAllChannels == isAllChannels) {
                store.accept(
                    ChannelsPageScreenEvent.Ui.Filter(ChannelsFilter(filter.text, !isAllChannels))
                )
            }
        }
    }

    private fun parseParams() {
        isAllChannels = arguments?.getBoolean(PARAM_IS_ALL_CHANNELS, false) ?: false
    }

    override fun onResume() {
        super.onResume()
        updateChannelsList()
    }

    private fun updateChannelsList() {
        if ((binding.recyclerViewChannels.adapter as MainDelegateAdapter).itemCount == NO_ITEMS) {
            store.accept(ChannelsPageScreenEvent.Ui.Load)
        }
        store.accept(ChannelsPageScreenEvent.Ui.UpdateMessageCount)
    }

    override fun onPause() {
        super.onPause()
        binding.shimmerLarge.off()
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