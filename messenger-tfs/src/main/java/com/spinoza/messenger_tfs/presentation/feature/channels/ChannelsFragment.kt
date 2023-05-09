package com.spinoza.messenger_tfs.presentation.feature.channels

import android.content.Context
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
import com.spinoza.messenger_tfs.di.channels.DaggerChannelsComponent
import com.spinoza.messenger_tfs.presentation.feature.channels.adapter.ChannelsPagerAdapter
import com.spinoza.messenger_tfs.presentation.feature.channels.model.ChannelsScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.channels.model.ChannelsScreenState
import com.spinoza.messenger_tfs.presentation.feature.channels.model.SearchQuery
import com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.ChannelsFragmentSharedViewModel
import com.spinoza.messenger_tfs.presentation.feature.channels.viewmodel.factory.ViewModelFactory
import com.spinoza.messenger_tfs.presentation.util.getAppComponent
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChannelsFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val sharedStore: ChannelsFragmentSharedViewModel by activityViewModels {
        viewModelFactory
    }

    private lateinit var onPageChangeCallback: ViewPager2.OnPageChangeCallback
    private var tabLayoutMediator: TabLayoutMediator? = null

    private var _binding: FragmentChannelsBinding? = null
    private val binding: FragmentChannelsBinding
        get() = _binding ?: throw RuntimeException("FragmentChannelsBinding == null")

    private val searchFilters = arrayListOf(EMPTY_FILTER, EMPTY_FILTER)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerChannelsComponent.factory().create(context.getAppComponent()).inject(this)
    }

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
            }.apply { attach() }
    }

    private fun setupListeners() {
        binding.editTextSearch.doOnTextChanged { text, _, _, _ ->
            sharedStore.accept(
                ChannelsScreenEvent.Ui.Filter(SearchQuery(binding.viewPager.currentItem, text))
            )
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                sharedStore.state.collect(::handleState)
            }
        }
    }

    private fun handleState(state: ChannelsScreenState) {
        state.filter?.let { filter ->
            if (filter.screenPosition == binding.viewPager.currentItem)
                searchFilters[filter.screenPosition] = filter.text
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(PARAM_SEARCH_FILTERS, searchFilters)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.tabLayout.removeAllTabs()
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        binding.viewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
        binding.viewPager.adapter = null
        _binding = null
    }

    companion object {

        private const val PARAM_SEARCH_FILTERS = "filters"
        private const val EMPTY_FILTER = ""
        private const val TAB_SUBSCRIBED = 0
        private const val TAB_ALL = 1

        fun newInstance(): ChannelsFragment {
            return ChannelsFragment()
        }
    }
}