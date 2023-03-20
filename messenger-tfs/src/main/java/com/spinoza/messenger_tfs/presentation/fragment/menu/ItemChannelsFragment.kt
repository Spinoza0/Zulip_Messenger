package com.spinoza.messenger_tfs.presentation.fragment.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.FragmentItemChannelsBinding
import com.spinoza.messenger_tfs.presentation.adapter.channels.ChannelsPagerAdapter

class ItemChannelsFragment : Fragment() {

    private var _binding: FragmentItemChannelsBinding? = null
    private val binding: FragmentItemChannelsBinding
        get() = _binding ?: throw RuntimeException("FragmentItemChannelsBinding == null")

    private lateinit var tabLayoutMediator: TabLayoutMediator

    private val channelsPagerAdapter by lazy {
        val childFragments = listOf(
            ChannelsPageFragment.newInstance(false),
            ChannelsPageFragment.newInstance(true)
        )
        ChannelsPagerAdapter(childFragmentManager, lifecycle, childFragments)
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