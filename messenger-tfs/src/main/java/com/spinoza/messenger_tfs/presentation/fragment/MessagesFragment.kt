package com.spinoza.messenger_tfs.presentation.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentMessagesBinding
import com.spinoza.messenger_tfs.domain.model.RepositoryState
import com.spinoza.messenger_tfs.domain.usecase.GetStateUseCase
import com.spinoza.messenger_tfs.domain.usecase.LoadMessagesUseCase
import com.spinoza.messenger_tfs.domain.usecase.SendMessageUseCase
import com.spinoza.messenger_tfs.domain.usecase.UpdateMessageUseCase
import com.spinoza.messenger_tfs.presentation.adapter.MessagesAdapter
import com.spinoza.messenger_tfs.presentation.viewmodel.MessageFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.MessageFragmentViewModelFactory

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding: FragmentMessagesBinding
        get() = _binding ?: throw RuntimeException("FragmentMessagesBinding == null")

    private val messagesAdapter = MessagesAdapter()

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            MessageFragmentViewModelFactory(
                GetStateUseCase(MessagesRepositoryImpl.getInstance()),
                LoadMessagesUseCase(MessagesRepositoryImpl.getInstance()),
                SendMessageUseCase(MessagesRepositoryImpl.getInstance()),
                UpdateMessageUseCase(MessagesRepositoryImpl.getInstance()),
            )
        )[MessageFragmentViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupScreen()
        setupObservers()
        setupListeners()
    }

    private fun setupScreen() {
        binding.toolbar.setNavigationOnClickListener {
            if (!findNavController().popBackStack()) {
                requireActivity().finish()
            }
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewMessages.adapter = messagesAdapter
    }

    private fun setupObservers() {
        viewModel.getState().observe(viewLifecycleOwner) { repositoryState ->
            when (repositoryState) {
                is RepositoryState.Messages -> {
                    messagesAdapter.submitList(repositoryState.messages) {
                        val lastPosition = messagesAdapter.itemCount - 1
                        binding.recyclerViewMessages.smoothScrollToPosition(lastPosition)
                    }
                }
                // TODO: show error
                else -> {}
            }
        }

        viewModel.messageActionIcon.observe(viewLifecycleOwner) {
            binding.imageViewAction.setImageResource(it)
        }
    }

    private fun setupListeners() {
        binding.imageViewAction.setOnClickListener {
            sendMessage()
        }

        binding.editTextMessage.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onMessageTextChanged(s)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun sendMessage() {
        if (viewModel.sendMessage(binding.editTextMessage.text.toString()))
            binding.editTextMessage.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}