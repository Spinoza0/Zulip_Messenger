package com.spinoza.messenger_tfs.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentChannelsBinding
import com.spinoza.messenger_tfs.domain.usecase.GetAllChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetSubscribedChannelsUseCase
import com.spinoza.messenger_tfs.presentation.adapter.MainAdapter
import com.spinoza.messenger_tfs.presentation.adapter.delegate.channel.ChannelFoldedDelegate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.channel.ChannelUnfoldedDelegate
import com.spinoza.messenger_tfs.presentation.adapter.toDelegateItems
import com.spinoza.messenger_tfs.presentation.model.ChannelsFragmentState
import com.spinoza.messenger_tfs.presentation.model.ChannelsFragmentState.SourceType
import com.spinoza.messenger_tfs.presentation.viewmodel.ChannelsFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.ChannelsFragmentViewModelFactory
import kotlinx.coroutines.launch

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

    private val viewModel: ChannelsFragmentViewModel by viewModels {
        ChannelsFragmentViewModelFactory(
            GetAllChannelsUseCase(MessagesRepositoryImpl.getInstance()),
            GetSubscribedChannelsUseCase(MessagesRepositoryImpl.getInstance()),
        )
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

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.textViewSubscribedStreams.setOnClickListener {
            viewModel.switchSource(SourceType.SUBSCRIBED)
        }
        binding.textViewAllStreams.setOnClickListener {
            viewModel.switchSource(SourceType.ALL)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.state.collect { newState ->
                    when (newState) {
                        is ChannelsFragmentState.Source -> handleSourceState(newState.type)
                        is ChannelsFragmentState.Channels -> mainAdapter.submitList(
                            newState.channels.toDelegateItems(viewModel::onChannelClickListener)
                        )

                        // TODO: show errors
                        is ChannelsFragmentState.Error -> {}
                    }
                }
            }
        }
    }

    private fun handleSourceState(type: SourceType) {
        when (type) {
            SourceType.SUBSCRIBED -> {
                binding.textViewSubscribedUnderline.visibility = View.VISIBLE
                binding.textViewAllUnderline.visibility = View.INVISIBLE
            }
            SourceType.ALL -> {
                binding.textViewSubscribedUnderline.visibility = View.INVISIBLE
                binding.textViewAllUnderline.visibility = View.VISIBLE
            }
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