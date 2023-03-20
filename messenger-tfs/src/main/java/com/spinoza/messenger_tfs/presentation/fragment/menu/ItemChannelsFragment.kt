package com.spinoza.messenger_tfs.presentation.fragment.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.spinoza.messenger_tfs.App
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentItemChannelsBinding
import com.spinoza.messenger_tfs.domain.model.ChannelFilter
import com.spinoza.messenger_tfs.domain.usecase.GetAllChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetSubscribedChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetTopicsUseCase
import com.spinoza.messenger_tfs.presentation.adapter.channels.ChannelsPagerAdapter
import com.spinoza.messenger_tfs.presentation.adapter.channels.TopicDelegateConfig
import com.spinoza.messenger_tfs.presentation.model.ChannelItem
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import com.spinoza.messenger_tfs.presentation.state.ChannelsScreenState
import com.spinoza.messenger_tfs.presentation.ui.getThemeColor
import com.spinoza.messenger_tfs.presentation.viewmodel.ChannelsFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.ChannelsFragmentViewModelFactory
import kotlinx.coroutines.flow.StateFlow

class ItemChannelsFragment : Fragment(), ChannelsPageParent {

    private var _binding: FragmentItemChannelsBinding? = null
    private val binding: FragmentItemChannelsBinding
        get() = _binding ?: throw RuntimeException("FragmentItemChannelsBinding == null")

    private lateinit var tabLayoutMediator: TabLayoutMediator

    private val viewModel: ChannelsFragmentViewModel by viewModels {
        val topicConfig = TopicDelegateConfig(
            requireContext().getString(R.string.channels_topic_template),
            requireContext().getThemeColor(R.attr.even_topic_color),
            requireContext().getThemeColor(R.attr.odd_topic_color),
            ::onTopicClickListener
        )
        ChannelsFragmentViewModelFactory(
            GetTopicsUseCase(MessagesRepositoryImpl.getInstance()),
            GetSubscribedChannelsUseCase(MessagesRepositoryImpl.getInstance()),
            GetAllChannelsUseCase(MessagesRepositoryImpl.getInstance()),
            ::onChannelClickListener,
            topicConfig
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentItemChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
    }

    private fun setupViewPager() {
        val childFragments = listOf(
            ChannelsPageFragment.newInstance(false),
            ChannelsPageFragment.newInstance(true)
        )

        val channelsPagerAdapter =
            ChannelsPagerAdapter(childFragmentManager, lifecycle, childFragments)
        binding.viewPager.adapter = channelsPagerAdapter

        tabLayoutMediator =
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                when (position) {
                    0 -> tab.setText(R.string.subscribed_streams)
                    1 -> tab.setText(R.string.all_streams)
                    else -> throw RuntimeException("Unknown tab position: $position")
                }
            }

        tabLayoutMediator.attach()
    }

    override fun loadItems(isAllChannels: Boolean) {
        viewModel.loadItems(isAllChannels)
    }

    override fun getState(isAllChannels: Boolean): StateFlow<ChannelsScreenState> {
        return if (isAllChannels) viewModel.stateAllItems else viewModel.stateSubscribedItems
    }

    private fun onChannelClickListener(channelItem: ChannelItem) {
        viewModel.onChannelClickListener(channelItem)
    }

    private fun onTopicClickListener(channelFilter: ChannelFilter) {
        App.router.navigateTo(Screens.Messages(channelFilter))
    }

    override fun onStop() {
        super.onStop()
        tabLayoutMediator.detach()
        binding.viewPager.adapter = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        fun newInstance(): ItemChannelsFragment {
            return ItemChannelsFragment()
        }
    }
}