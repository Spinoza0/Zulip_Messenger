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
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentMessagesBinding
import com.spinoza.messenger_tfs.domain.model.RepositoryState
import com.spinoza.messenger_tfs.domain.usecase.GetStateUseCase
import com.spinoza.messenger_tfs.domain.usecase.LoadMessagesUseCase
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
                LoadMessagesUseCase(MessagesRepositoryImpl.getInstance())
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
    }

    private fun setupScreen() {
        binding.toolbar.setNavigationOnClickListener {
            if (!findNavController().popBackStack()) {
                requireActivity().finish()
            }
        }

        binding.editTextMessage.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setMessageActionImage()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewMessages.adapter = messagesAdapter
    }

    private fun setupObservers() {
        viewModel.getState().observe(viewLifecycleOwner) { repositoryState ->
            when (repositoryState) {
                is RepositoryState.Messages -> messagesAdapter.submitList(repositoryState.messages)
                // TODO: show error
                else -> {}
            }
        }
    }

    private fun setMessageActionImage() {
        if (isMessageEmpty()) {
            binding.imageViewAction.setImageResource(R.drawable.ic_add_circle_outline)
        } else {
            binding.imageViewAction.setImageResource(R.drawable.ic_send)
        }
    }

    private fun isMessageEmpty(): Boolean {
        return binding.editTextMessage.text.toString().trim().isEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}