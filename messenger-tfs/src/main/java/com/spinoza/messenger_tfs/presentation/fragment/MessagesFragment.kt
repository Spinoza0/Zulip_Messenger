package com.spinoza.messenger_tfs.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.spinoza.messenger_tfs.MessengerApp
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentMessagesBinding
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.domain.model.RepositoryState
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.adapter.MainAdapter
import com.spinoza.messenger_tfs.presentation.adapter.delegate.date.DateDelegate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.message.CompanionMessageDelegate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.message.UserMessageDelegate
import com.spinoza.messenger_tfs.presentation.adapter.groupByDate
import com.spinoza.messenger_tfs.presentation.adapter.itemdecorator.StickyDateInHeaderItemDecoration
import com.spinoza.messenger_tfs.presentation.cicerone.Screens
import com.spinoza.messenger_tfs.presentation.ui.MessageView
import com.spinoza.messenger_tfs.presentation.ui.getThemeColor
import com.spinoza.messenger_tfs.presentation.ui.smoothScrollToChangedMessage
import com.spinoza.messenger_tfs.presentation.viewmodel.MessagesFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.MessagesFragmentViewModelFactory
import kotlinx.coroutines.launch

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding: FragmentMessagesBinding
        get() = _binding ?: throw RuntimeException("FragmentMessagesBinding == null")

    private val mainAdapter: MainAdapter by lazy {
        MainAdapter().apply {
            addDelegate(CompanionMessageDelegate())
            addDelegate(UserMessageDelegate())
            addDelegate(DateDelegate())
        }
    }

    private val viewModel: MessagesFragmentViewModel by viewModels {
        MessagesFragmentViewModelFactory(
            GetRepositoryStateUseCase(MessagesRepositoryImpl.getInstance()),
            SendMessageUseCase(MessagesRepositoryImpl.getInstance()),
            UpdateReactionUseCase(MessagesRepositoryImpl.getInstance()),
        )
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            openMainFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            onBackPressedCallback
        )

        setupScreen()
    }

    private fun setupScreen() {
        setupStatusBar()
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupStatusBar() {
        requireActivity().window.statusBarColor =
            requireContext().getThemeColor(R.attr.channel_toolbar_background_color)
    }

    private fun setupRecyclerView() {
        binding.recyclerViewMessages.adapter = mainAdapter
        binding.recyclerViewMessages.addItemDecoration(StickyDateInHeaderItemDecoration())
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.state
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect(::handleState)
        }
    }

    private fun handleState(state: RepositoryState) {
        when (state) {
            is RepositoryState.Messages -> submitMessages(state.messages) {
                when (state.position.type) {
                    MessagePosition.Type.LAST_POSITION -> {
                        val lastItemPosition = mainAdapter.itemCount - 1
                        binding.recyclerViewMessages.smoothScrollToPosition(lastItemPosition)
                    }
                    MessagePosition.Type.EXACTLY -> {
                        binding.recyclerViewMessages.smoothScrollToChangedMessage(
                            state.position.messageId
                        )
                    }
                    else -> {}
                }
            }
            // TODO: show error
            is RepositoryState.Error -> {}
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            openMainFragment()
        }

        binding.imageViewAction.setOnClickListener {
            sendMessage()
        }

        binding.editTextMessage.doOnTextChanged { text, _, _, _ ->
            changeButtonSendIcon(text != null && text.toString().trim().isNotEmpty())
        }
    }

    private fun onReactionAddClickListener(messageView: MessageView) {
        val dialog = ChooseReactionDialogFragment.newInstance(
            messageView.messageId,
            viewModel.getUserId()
        )
        dialog.listener = viewModel::updateReaction
        dialog.show(requireActivity().supportFragmentManager, ChooseReactionDialogFragment.TAG)
    }

    // TODO: логику перенести во viewModel
    private fun changeButtonSendIcon(readyToSend: Boolean) {
        val resId = if (readyToSend)
            R.drawable.ic_send
        else
            R.drawable.ic_add_circle_outline
        binding.imageViewAction.setImageResource(resId)
    }

    private fun sendMessage() {
        if (viewModel.sendMessage(binding.editTextMessage.text.toString())) {
            binding.editTextMessage.text?.clear()
        }
    }

    private fun submitMessages(messages: List<Message>, callbackAfterSubmit: () -> Unit) {
        mainAdapter.submitList(
            messages.groupByDate(
                userId = viewModel.getUserId(),
                onReactionAddClickListener = ::onReactionAddClickListener,
                onReactionClickListener = viewModel::updateReaction
            )
        ) {
            callbackAfterSubmit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openMainFragment() {
        MessengerApp.router.replaceScreen(Screens.Main())
    }

    companion object {

        fun newInstance(): MessagesFragment {
            return MessagesFragment()
        }
    }
}