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
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.usecase.GetStateUseCase
import com.spinoza.messenger_tfs.domain.usecase.LoadMessagesUseCase
import com.spinoza.messenger_tfs.domain.usecase.SendMessageUseCase
import com.spinoza.messenger_tfs.presentation.adapter.MainAdapter
import com.spinoza.messenger_tfs.presentation.adapter.date.DateDelegate
import com.spinoza.messenger_tfs.presentation.adapter.itemdecorator.StickyDateInHeaderItemDecoration
import com.spinoza.messenger_tfs.presentation.adapter.message.CompanionMessageDelegate
import com.spinoza.messenger_tfs.presentation.adapter.message.UserMessageDelegate
import com.spinoza.messenger_tfs.presentation.adapter.utils.groupByDate
import com.spinoza.messenger_tfs.presentation.ui.MessageView
import com.spinoza.messenger_tfs.presentation.ui.getThemeColor
import com.spinoza.messenger_tfs.presentation.viewmodel.MessageFragmentViewModelFactory
import com.spinoza.messenger_tfs.presentation.viewmodel.MessagesFragmentViewModel

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding: FragmentMessagesBinding
        get() = _binding ?: throw RuntimeException("FragmentMessagesBinding == null")

    private val currentUser = User(TEST_USER_ID, TEST_USER_NAME, TEST_USER_AVATAR)

    private val mainAdapter: MainAdapter by lazy {
        MainAdapter().apply {
            addDelegate(CompanionMessageDelegate())
            addDelegate(UserMessageDelegate())
            addDelegate(DateDelegate())
        }
    }

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            MessageFragmentViewModelFactory(
                GetStateUseCase(MessagesRepositoryImpl.getInstance()),
                LoadMessagesUseCase(MessagesRepositoryImpl.getInstance()),
                SendMessageUseCase(MessagesRepositoryImpl.getInstance()),
            )
        )[MessagesFragmentViewModel::class.java]
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
    }

    private fun setupScreen() {
        binding.toolbar.setNavigationOnClickListener {
            if (!findNavController().popBackStack()) {
                requireActivity().finish()
            }
        }

        setupStatusBar()
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewMessages.adapter = mainAdapter
        binding.recyclerViewMessages.addItemDecoration(StickyDateInHeaderItemDecoration())
    }

    private fun setupObservers() {
        viewModel.getState().observe(viewLifecycleOwner) { repositoryState ->
            when (repositoryState) {
                is RepositoryState.Messages -> {
                    mainAdapter.submitList(
                        repositoryState.messages.groupByDate(
                            currentUser,
                            ::onAvatarLongClickListener,
                            ::onReactionAddClickListener,
                            ::onReactionClickListener
                        )
                    ) {
                        val lastPosition = mainAdapter.itemCount - 1
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

    private fun setupStatusBar() {
        requireActivity().window.statusBarColor =
            requireContext().getThemeColor(R.attr.channel_toolbar_background_color)
    }

    private fun sendMessage() {
        if (viewModel.sendMessage(binding.editTextMessage.text.toString(), currentUser))
            binding.editTextMessage.text?.clear()
    }

    private fun onAvatarLongClickListener(messageView: MessageView) {
        // TODO: onAvatarLongClickListener
    }

    private fun onReactionAddClickListener(messageView: MessageView) {
        val action =
            MessagesFragmentDirections.actionMessagesFragmentToAddReactionFragment(
                messageView.messageId
            )
        findNavController().navigate(action)
    }

    private fun onReactionClickListener(messageView: MessageView) {
        // TODO: onReactionClickListener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val TEST_USER_ID = 100
        const val TEST_USER_NAME = "Test User Name"
        const val TEST_USER_AVATAR = R.drawable.test_face
    }
}