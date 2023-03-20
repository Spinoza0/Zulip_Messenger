package com.spinoza.messenger_tfs.presentation.fragment.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.spinoza.messenger_tfs.databinding.FragmentChannelsPageBinding
import com.spinoza.messenger_tfs.presentation.adapter.channels.ChannelDelegate
import com.spinoza.messenger_tfs.presentation.adapter.channels.TopicDelegate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.MainDelegateAdapter
import com.spinoza.messenger_tfs.presentation.state.ChannelsScreenState
import com.spinoza.messenger_tfs.presentation.ui.off
import com.spinoza.messenger_tfs.presentation.ui.on
import kotlinx.coroutines.launch

class ChannelsPageFragment : Fragment() {

    private lateinit var channelsPageParent: ChannelsPageParent
    private var isAllChannels = false

    private var _binding: FragmentChannelsPageBinding? = null
    private val binding: FragmentChannelsPageBinding
        get() = _binding ?: throw RuntimeException("FragmentChannelsPageBinding == null")

    private val delegatesAdapter by lazy {
        MainDelegateAdapter().apply {
            addDelegate(ChannelDelegate())
            addDelegate(TopicDelegate())
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

        channelsPageParent = parentFragment as ChannelsPageParent
        setupRecyclerView()
        parseParams()
        setupObservers()

        if (savedInstanceState == null) {
            channelsPageParent.loadItems(isAllChannels)
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewChannels.adapter = delegatesAdapter
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                channelsPageParent.getState(isAllChannels).collect(::handleState)
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

    private fun parseParams() {
        isAllChannels = arguments?.getBoolean(PARAM_IS_ALL_CHANNELS, false) ?: false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        private const val PARAM_IS_ALL_CHANNELS = "isAllChannels"

        fun newInstance(isAllChannels: Boolean): ChannelsPageFragment {
            return ChannelsPageFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(PARAM_IS_ALL_CHANNELS, isAllChannels)
                }
            }
        }
    }
}