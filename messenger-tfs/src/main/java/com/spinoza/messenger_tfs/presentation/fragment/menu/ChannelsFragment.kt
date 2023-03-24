package com.spinoza.messenger_tfs.presentation.fragment.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayoutMediator
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.FragmentItemChannelsBinding
import com.spinoza.messenger_tfs.presentation.adapter.channels.ChannelsPagerAdapter
import com.spinoza.messenger_tfs.presentation.state.ChannelsScreenState
import com.spinoza.messenger_tfs.presentation.viewmodel.ChannelsFragmentViewModel
import kotlinx.coroutines.launch

class ChannelsFragment : Fragment() {

    private var _binding: FragmentItemChannelsBinding? = null
    private val binding: FragmentItemChannelsBinding
        get() = _binding ?: throw RuntimeException("FragmentItemChannelsBinding == null")

    private lateinit var tabLayoutMediator: TabLayoutMediator

    private val fragments = listOf(
        ChannelsPageFragment.newInstance(false),
        ChannelsPageFragment.newInstance(true)
    )

    private val viewModel: ChannelsFragmentViewModel by viewModels()

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
        setupListeners()
        setupObservers()
    }

    private fun setupViewPager() {
        val channelsPagerAdapter = ChannelsPagerAdapter(childFragmentManager, lifecycle, fragments)
        binding.viewPager.adapter = channelsPagerAdapter

        tabLayoutMediator =
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                when (position) {
                    TAB_SUBSCRIBED -> tab.setText(R.string.subscribed_streams)
                    TAB_ALL -> tab.setText(R.string.all_streams)
                    else -> throw RuntimeException("Unknown tab position: $position")
                }
            }
        tabLayoutMediator.attach()
    }

    private fun setupListeners() {
        binding.editTextSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.doOnTextChanged(text)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.state.collect(::handleState)
            }
        }
    }

    private fun handleState(state: ChannelsScreenState) {
        when (state) {
            is ChannelsScreenState.Search ->
                fragments[binding.viewPager.currentItem].setChannelsFilter(state.value)
            is ChannelsScreenState.Idle -> {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tabLayoutMediator.detach()
        binding.viewPager.adapter = null
        _binding = null
    }

    companion object {

        private const val TAB_SUBSCRIBED = 0
        private const val TAB_ALL = 1

        fun newInstance(): ChannelsFragment {
            return ChannelsFragment()
        }
    }
}