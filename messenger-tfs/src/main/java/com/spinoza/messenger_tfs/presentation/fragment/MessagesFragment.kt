package com.spinoza.messenger_tfs.presentation.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.spinoza.messenger_tfs.MessengerApp
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentMessagesBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.domain.repository.RepositoryState
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.adapter.MainAdapter
import com.spinoza.messenger_tfs.presentation.adapter.delegate.date.DateDelegate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.message.CompanionMessageDelegate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.message.UserMessageDelegate
import com.spinoza.messenger_tfs.presentation.adapter.groupByDate
import com.spinoza.messenger_tfs.presentation.adapter.itemdecorator.StickyDateInHeaderItemDecoration
import com.spinoza.messenger_tfs.presentation.model.MessagesFragmentState
import com.spinoza.messenger_tfs.presentation.ui.*
import com.spinoza.messenger_tfs.presentation.viewmodel.MessagesFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.MessagesFragmentViewModelFactory
import kotlinx.coroutines.launch

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding: FragmentMessagesBinding
        get() = _binding ?: throw RuntimeException("FragmentMessagesBinding == null")

    private lateinit var channel: Channel
    private lateinit var topicName: String

    private val mainAdapter: MainAdapter by lazy {
        MainAdapter().apply {
            addDelegate(CompanionMessageDelegate())
            addDelegate(UserMessageDelegate())
            addDelegate(DateDelegate())
        }
    }

    private val viewModel: MessagesFragmentViewModel by viewModels {
        MessagesFragmentViewModelFactory(
            GetCurrentUserUseCase(MessagesRepositoryImpl.getInstance()),
            GetMessagesUseCase(MessagesRepositoryImpl.getInstance()),
            SendMessageUseCase(MessagesRepositoryImpl.getInstance()),
            UpdateReactionUseCase(MessagesRepositoryImpl.getInstance()),
            channel,
            topicName
        )
    }

    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goBack()
            }
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

        parseParams()
        setupScreen()
    }

    private fun setupScreen() {
        setupStatusBar()
        setupRecyclerView()
        setupObservers()
        setupListeners()

        binding.textViewTopic.text =
            String.format(getString(R.string.messages_topic_template), topicName)
    }

    private fun setupStatusBar() {
        binding.toolbar.title = "#${channel.name}"
        requireActivity().window.statusBarColor =
            requireContext().getThemeColor(R.attr.channel_toolbar_background_color)
    }

    private fun setupRecyclerView() {
        binding.recyclerViewMessages.adapter = mainAdapter
        binding.recyclerViewMessages.addItemDecoration(StickyDateInHeaderItemDecoration())
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::handleMessagesFragmentState)
            }
        }
    }

    private fun handleMessagesFragmentState(state: MessagesFragmentState) {
        if (state !is MessagesFragmentState.Loading) {
            binding.progressBar.off()
        }
        when (state) {
            is MessagesFragmentState.Repository -> handleRepositoryState(state.state)
            is MessagesFragmentState.SendIconImage -> {
                binding.imageViewAction.setImageResource(state.resId)
            }
            is MessagesFragmentState.Loading -> binding.progressBar.on()
        }
    }

    private fun handleRepositoryState(state: RepositoryState) {
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
            is RepositoryState.Error -> {
                when (state.type) {
                    RepositoryState.ErrorType.USER_WITH_ID_NOT_FOUND -> {
                        val text = String.format(
                            getString(R.string.error_user_not_found),
                            state.text
                        )
                        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
                    }
                }
            }
            else -> {}
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener {
            goBack()
        }

        binding.imageViewAction.setOnClickListener {
            sendMessage()
        }

        binding.editTextMessage.doOnTextChanged { text, _, _, _ ->
            viewModel.doOnTextChanged(text)
        }
    }

    private fun onReactionAddClickListener(messageView: MessageView) {
        val dialog = ChooseReactionDialogFragment.newInstance(
            messageView.messageId,
        )
        dialog.listener = viewModel::updateReaction
        dialog.show(requireActivity().supportFragmentManager, ChooseReactionDialogFragment.TAG)
    }

    private fun sendMessage() {
        if (viewModel.sendMessage(binding.editTextMessage.text.toString())) {
            binding.editTextMessage.text?.clear()
        }
    }

    private fun submitMessages(messages: List<Message>, callbackAfterSubmit: () -> Unit) {
        mainAdapter.submitList(
            messages.groupByDate(
                user = viewModel.user,
                onReactionAddClickListener = ::onReactionAddClickListener,
                onReactionClickListener = viewModel::updateReaction
            )
        ) {
            callbackAfterSubmit()
        }
    }

    override fun onResume() {
        super.onResume()

        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            onBackPressedCallback
        )

        viewModel.doOnTextChanged(binding.editTextMessage.text)
        viewModel.getMessages()
    }

    override fun onPause() {
        super.onPause()
        onBackPressedCallback.remove()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun goBack() {
        MessengerApp.router.exit()
    }

    @Suppress("deprecation")
    private fun parseParams() {
        topicName = arguments?.getString(EXTRA_TOPIC_NAME) ?: EMPTY_STRING
        val newChannel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(EXTRA_CHANNEL, Channel::class.java)
        } else {
            arguments?.getParcelable(EXTRA_CHANNEL)
        }

        if (newChannel == null || newChannel.channelId == Channel.UNDEFINED_ID ||
            topicName.isEmpty()
        ) {
            goBack()
        } else {
            channel = newChannel
        }
    }

    companion object {

        private const val EMPTY_STRING = ""
        private const val EXTRA_CHANNEL = "channel"
        private const val EXTRA_TOPIC_NAME = "topic"

        fun newInstance(channel: Channel, topicName: String): MessagesFragment {
            return MessagesFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_CHANNEL, channel)
                    putString(EXTRA_TOPIC_NAME, topicName)
                }
            }
        }
    }
}