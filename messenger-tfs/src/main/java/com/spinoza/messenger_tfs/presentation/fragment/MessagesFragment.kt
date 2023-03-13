package com.spinoza.messenger_tfs.presentation.fragment

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentMessagesBinding
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagesState
import com.spinoza.messenger_tfs.domain.usecase.GetMessagesStateUseCase
import com.spinoza.messenger_tfs.domain.usecase.LoadMessagesUseCase
import com.spinoza.messenger_tfs.domain.usecase.SendMessageUseCase
import com.spinoza.messenger_tfs.domain.usecase.UpdateReactionUseCase
import com.spinoza.messenger_tfs.presentation.adapter.MainAdapter
import com.spinoza.messenger_tfs.presentation.adapter.delegate.date.DateDelegate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.message.CompanionMessageDelegate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.message.UserMessageDelegate
import com.spinoza.messenger_tfs.presentation.adapter.itemdecorator.StickyDateInHeaderItemDecoration
import com.spinoza.messenger_tfs.presentation.adapter.utils.groupByDate
import com.spinoza.messenger_tfs.presentation.ui.MessageView
import com.spinoza.messenger_tfs.presentation.ui.ReactionView
import com.spinoza.messenger_tfs.presentation.ui.getThemeColor
import com.spinoza.messenger_tfs.presentation.viewmodel.MessagesFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.MessageFragmentViewModelFactory
import kotlinx.coroutines.launch

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding: FragmentMessagesBinding
        get() = _binding ?: throw RuntimeException("FragmentMessagesBinding == null")

    private val currentUserId = TEST_USER_ID

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
                GetMessagesStateUseCase(MessagesRepositoryImpl.getInstance()),
                LoadMessagesUseCase(MessagesRepositoryImpl.getInstance()),
                SendMessageUseCase(MessagesRepositoryImpl.getInstance()),
                UpdateReactionUseCase(MessagesRepositoryImpl.getInstance()),
            )
        )[MessagesFragmentViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
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
        viewModel.loadMessages(currentUserId)
    }

    private fun setupRecyclerView() {
        binding.recyclerViewMessages.adapter = mainAdapter
        binding.recyclerViewMessages.addItemDecoration(StickyDateInHeaderItemDecoration())
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.state
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { state ->
                    when (state) {
                        is MessagesState.ReadyToSend -> changeIconSendState(state.status)
                        is MessagesState.Messages -> submitMessages(state.messages) { }
                        is MessagesState.MessageSent -> submitMessages(state.messages) {
                            smoothScrollToLastPosition()
                        }
                        is MessagesState.MessageChanged -> submitMessages(state.messages) {
                            showChangedMessage(state.changedMessageId)
                        }
                        // TODO: show error
                        is MessagesState.Error -> {}
                    }
                }
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
        if (viewModel.sendMessage(binding.editTextMessage.text.toString(), currentUserId)) {
            binding.editTextMessage.text?.clear()
            val inputMethodManager =
                requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.editTextMessage.windowToken, 0)
        }
    }

    private fun onReactionAddClickListener(messageView: MessageView) {
        val action =
            MessagesFragmentDirections.actionMessagesFragmentToAddReactionFragment(
                messageView.messageId,
                currentUserId
            )
        findNavController().navigate(action)
    }

    private fun onReactionClickListener(messageView: MessageView, reactionView: ReactionView) {
        viewModel.updateReaction(messageView, currentUserId, reactionView)
    }

    private fun submitMessages(messages: List<Message>, callbackAfterSubmit: () -> Unit) {
        mainAdapter.submitList(
            messages.groupByDate(
                userId = currentUserId,
                onReactionAddClickListener = ::onReactionAddClickListener,
                onReactionClickListener = ::onReactionClickListener
            )
        ) {
            callbackAfterSubmit()
        }
    }

    private fun smoothScrollToLastPosition() {
        val lastPosition = mainAdapter.itemCount - 1
        val layoutManager =
            binding.recyclerViewMessages.layoutManager as LinearLayoutManager
        val lastVisiblePosition =
            layoutManager.findLastVisibleItemPosition()
        if (lastVisiblePosition != RecyclerView.NO_POSITION &&
            (lastPosition - lastVisiblePosition) > MAX_DISTANCE
        ) {
            binding.recyclerViewMessages.scrollToPosition(lastPosition - MAX_DISTANCE)
        }
        binding.recyclerViewMessages.smoothScrollToPosition(lastPosition)
    }

    private fun changeIconSendState(status: Boolean) {
        val resId = if (status)
            R.drawable.ic_send
        else
            R.drawable.ic_add_circle_outline
        binding.imageViewAction.setImageResource(resId)
    }

    private fun showChangedMessage(changedMessageId: Int) {
        if (changedMessageId == Message.UNDEFINED_ID) return

        var position = RecyclerView.NO_POSITION
        for (i in 0 until binding.recyclerViewMessages.childCount) {
            val view = binding.recyclerViewMessages.getChildAt(i)
            val item = view.findViewById<MessageView>(R.id.messageView)
            if (item != null && item.messageId == changedMessageId) {
                position = binding.recyclerViewMessages.getChildAdapterPosition(view)
                break
            }
        }

        if (position != RecyclerView.NO_POSITION) {
            val layoutManager =
                binding.recyclerViewMessages.layoutManager as LinearLayoutManager
            val firstCompletelyVisiblePosition =
                layoutManager.findFirstCompletelyVisibleItemPosition()
            val lastCompletelyVisiblePosition =
                layoutManager.findLastCompletelyVisibleItemPosition()
            if (position < firstCompletelyVisiblePosition ||
                position >= lastCompletelyVisiblePosition
            ) {
                binding.recyclerViewMessages.smoothScrollToPosition(position)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val TEST_USER_ID = 100
        const val MAX_DISTANCE = 10
    }
}