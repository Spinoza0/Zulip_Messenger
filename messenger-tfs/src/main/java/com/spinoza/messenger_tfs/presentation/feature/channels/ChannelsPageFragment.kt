package com.spinoza.messenger_tfs.presentation.feature.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.FragmentChannelsPageBinding
import com.spinoza.messenger_tfs.di.channels.DaggerChannelsComponent
import com.spinoza.messenger_tfs.domain.model.ChannelsFilter
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.presentation.feature.app.adapter.MainDelegateAdapter
import com.spinoza.messenger_tfs.presentation.feature.app.utils.closeApplication
import com.spinoza.messenger_tfs.presentation.feature.app.utils.getAppComponent
import com.spinoza.messenger_tfs.presentation.feature.app.utils.showCheckInternetConnectionDialog
import com.spinoza.messenger_tfs.presentation.feature.app.utils.showError
import com.spinoza.messenger_tfs.presentation.feature.channels.adapter.ChannelDelegate
import com.spinoza.messenger_tfs.presentation.feature.channels.adapter.TopicDelegate
import com.spinoza.messenger_tfs.presentation.feature.channels.model.*
import com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.ChannelsFragmentSharedViewModel
import com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.ChannelsPageFragmentViewModel
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.getThemeColor
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.off
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.on
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChannelsPageFragment : Fragment() {

    @Inject
    lateinit var channelsAdapter: MainDelegateAdapter

    @Inject
    lateinit var store: ChannelsPageFragmentViewModel

    @Inject
    lateinit var sharedStore: ChannelsFragmentSharedViewModel

    private var isSubscribed = true
    private var _binding: FragmentChannelsPageBinding? = null
    private val binding: FragmentChannelsPageBinding
        get() = _binding ?: throw RuntimeException("FragmentChannelsPageBinding == null")

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
        DaggerChannelsComponent.factory()
            .create(requireContext().getAppComponent(), requireActivity(), this, isSubscribed)
            .inject(this)
        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        channelsAdapter.addDelegate(
            ChannelDelegate(
                getString(R.string.channel_name_template),
                ::onChannelClickListener
            )
        )
        channelsAdapter.addDelegate(
            TopicDelegate(
                requireContext().getThemeColor(R.attr.even_topic_color),
                requireContext().getThemeColor(R.attr.odd_topic_color),
                ::onTopicClickListener
            )
        )
        binding.recyclerViewChannels.adapter = channelsAdapter
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
            channelsAdapter.submitList(it)
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
            val filterIsSubscribed = filter.screenPosition % 2 == 0
            if (filterIsSubscribed == isSubscribed) {
                store.accept(
                    ChannelsPageScreenEvent.Ui.Filter(ChannelsFilter(filter.text, isSubscribed))
                )
            }
        }
    }

    private fun parseParams() {
        isSubscribed = arguments?.getBoolean(PARAM_IS_SUBSCRIBED, true) ?: true
    }

    override fun onResume() {
        super.onResume()
        updateChannelsList()
    }

    private fun updateChannelsList() {
        if (channelsAdapter.itemCount == NO_ITEMS) {
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
        channelsAdapter.clear()
        binding.recyclerViewChannels.adapter = null
        _binding = null
    }

    companion object {

        private const val PARAM_IS_SUBSCRIBED = "isSubscribed"
        private const val NO_ITEMS = 0

        fun newInstance(isSubscribed: Boolean): ChannelsPageFragment {
            return ChannelsPageFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(PARAM_IS_SUBSCRIBED, isSubscribed)
                }
            }
        }
    }
}