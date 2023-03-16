package com.spinoza.messenger_tfs.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.spinoza.messenger_tfs.databinding.FragmentChannelsBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.presentation.adapter.MainAdapter
import com.spinoza.messenger_tfs.presentation.adapter.delegate.channel.ChannelFoldedDelegate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.channel.ChannelUnfoldedDelegate
import com.spinoza.messenger_tfs.presentation.adapter.toDelegateItems

class ChannelsFragment : Fragment() {

    private var _binding: FragmentChannelsBinding? = null
    private val binding: FragmentChannelsBinding
        get() = _binding ?: throw RuntimeException("FragmentChannelsBinding == null")

    private val mainAdapter: MainAdapter by lazy {
        MainAdapter().apply {
            addDelegate(ChannelFoldedDelegate())
            addDelegate(ChannelUnfoldedDelegate())
        }
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

        setupScreen()
    }

    private fun setupScreen() {
        binding.recyclerViewChannels.adapter = mainAdapter
    }

    private fun submitChannel(channels: List<Channel>, callbackAfterSubmit: () -> Unit) {
        mainAdapter.submitList(channels.toDelegateItems()) {
            callbackAfterSubmit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): ChannelsFragment {
            return ChannelsFragment()
        }
    }
}