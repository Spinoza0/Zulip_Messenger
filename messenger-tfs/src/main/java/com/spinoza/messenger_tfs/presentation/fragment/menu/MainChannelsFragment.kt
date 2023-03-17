package com.spinoza.messenger_tfs.presentation.fragment.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.FragmentMainChannelsBinding
import com.spinoza.messenger_tfs.presentation.adapter.ViewPagerAdapter

class MainChannelsFragment : Fragment() {

    private var _binding: FragmentMainChannelsBinding? = null
    private val binding: FragmentMainChannelsBinding
        get() = _binding ?: throw RuntimeException("FragmentMainChannelsBinding == null")

    private val childFragments = listOf(
        ChannelsPageFragment.newInstance(false),
        ChannelsPageFragment.newInstance(true)
    )

    private val viewPagerAdapter by lazy {
        ViewPagerAdapter(requireActivity().supportFragmentManager, lifecycle, childFragments)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMainChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabLayoutMediator()
    }

    private fun setupTabLayoutMediator() {
        binding.viewPager.adapter = viewPagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.setText(R.string.subscribed_streams)
                1 -> tab.setText(R.string.all_streams)
                else -> throw RuntimeException("Unknown tab position: $position")
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        fun newInstance(): MainChannelsFragment {
            return MainChannelsFragment()
        }
    }
}