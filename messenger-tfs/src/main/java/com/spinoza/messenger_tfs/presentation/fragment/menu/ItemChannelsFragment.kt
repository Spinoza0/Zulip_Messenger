package com.spinoza.messenger_tfs.presentation.fragment.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.ChannelItemBinding
import com.spinoza.messenger_tfs.databinding.FragmentItemChannelsBinding
import com.spinoza.messenger_tfs.domain.usecase.GetAllChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetSubscribedChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetTopicsUseCase
import com.spinoza.messenger_tfs.presentation.adapter.ViewPagerAdapter
import com.spinoza.messenger_tfs.presentation.model.ChannelItem
import com.spinoza.messenger_tfs.presentation.model.ChannelsFragmentState
import com.spinoza.messenger_tfs.presentation.viewmodel.ChannelsFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.ChannelsViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.ChannelsFragmentViewModelFactory
import kotlinx.coroutines.flow.StateFlow

class ItemChannelsFragment : Fragment(), ChannelsViewModel {

    private var _binding: FragmentItemChannelsBinding? = null
    private val binding: FragmentItemChannelsBinding
        get() = _binding ?: throw RuntimeException("FragmentItemChannelsBinding == null")


    private val viewModel: ChannelsFragmentViewModel by viewModels {
        ChannelsFragmentViewModelFactory(
            GetTopicsUseCase(MessagesRepositoryImpl.getInstance()),
            GetSubscribedChannelsUseCase(MessagesRepositoryImpl.getInstance()),
            GetAllChannelsUseCase(MessagesRepositoryImpl.getInstance())
        )
    }

    private lateinit var tabLayoutMediator: TabLayoutMediator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentItemChannelsBinding.inflate(inflater, container, false)
        setupViewPager()
        return binding.root
    }

    private fun setupViewPager() {
        val childFragments = listOf(
            ChannelsPageFragment.newInstance(false),
            ChannelsPageFragment.newInstance(true)
        )

        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager, lifecycle, childFragments)
        binding.viewPager.adapter = viewPagerAdapter

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

    override fun getChannels(allChannels: Boolean) {
        viewModel.getChannels(allChannels)
    }

    override fun getState(allChannels: Boolean): StateFlow<ChannelsFragmentState> {
        return if (allChannels) viewModel.stateAllChannels else viewModel.stateSubscribedChannels
    }

    override fun onChannelClickListener(
        allChannels: Boolean,
        channelItem: ChannelItem,
        itemBinding: ChannelItemBinding,
    ) {
        viewModel.onChannelClickListener(allChannels, channelItem, itemBinding)
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