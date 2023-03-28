package com.spinoza.messenger_tfs.presentation.fragment.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.FragmentChannelsBinding
import com.spinoza.messenger_tfs.presentation.adapter.channels.ChannelsPagerAdapter
import com.spinoza.messenger_tfs.presentation.model.SearchQuery
import com.spinoza.messenger_tfs.presentation.state.ChannelsScreenState
import com.spinoza.messenger_tfs.presentation.viewmodel.ChannelsFragmentSharedViewModel
import kotlinx.coroutines.launch

class ChannelsFragment : Fragment() {

    private var _binding: FragmentChannelsBinding? = null
    private val binding: FragmentChannelsBinding
        get() = _binding ?: throw RuntimeException("FragmentChannelsBinding == null")

    private val sharedViewModel: ChannelsFragmentSharedViewModel by activityViewModels()
    private val searchFilters = arrayListOf("", "")

    private lateinit var tabLayoutMediator: TabLayoutMediator
    private lateinit var onPageChangeCallback: ViewPager2.OnPageChangeCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            val savedList = savedInstanceState.getStringArrayList(PARAM_SEARCH_FILTERS)
            savedList?.let {
                searchFilters[TAB_SUBSCRIBED] = savedList[TAB_SUBSCRIBED]
                searchFilters[TAB_ALL] = savedList[TAB_ALL]
            }
        }

        setupListeners()
        setupObservers()
        setupViewPager()
    }

    private fun setupViewPager() {
        val channelsPagerAdapter = ChannelsPagerAdapter(this)
        onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.editTextSearch.setText(searchFilters[position])
            }
        }
        binding.viewPager.registerOnPageChangeCallback(onPageChangeCallback)
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
            sharedViewModel.doOnTextChanged(SearchQuery(binding.viewPager.currentItem, text))
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                sharedViewModel.state.collect(::handleState)
            }
        }
    }

    private fun handleState(state: ChannelsScreenState) {
        when (state) {
            is ChannelsScreenState.Filter -> {
                if (state.value.screenPosition == binding.viewPager.currentItem)
                    searchFilters[state.value.screenPosition] = state.value.text
            }
            is ChannelsScreenState.Idle -> {}
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putStringArrayList(PARAM_SEARCH_FILTERS, searchFilters)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tabLayoutMediator.detach()
        binding.viewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
        binding.viewPager.adapter = null
        _binding = null
    }

    companion object {

        private const val PARAM_SEARCH_FILTERS = "filters"
        private const val TAB_SUBSCRIBED = 0
        private const val TAB_ALL = 1

        fun newInstance(): ChannelsFragment {
            return ChannelsFragment()
        }
    }
}