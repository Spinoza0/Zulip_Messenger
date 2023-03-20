package com.spinoza.messenger_tfs.presentation.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.spinoza.messenger_tfs.App
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentMessagesBinding
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.repository.MessagePosition
import com.spinoza.messenger_tfs.domain.repository.MessagesResult
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.adapter.message.DelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.MessagesAdapter
import com.spinoza.messenger_tfs.presentation.adapter.message.StickyDateInHeaderItemDecoration
import com.spinoza.messenger_tfs.presentation.adapter.message.date.DateDelegate
import com.spinoza.messenger_tfs.presentation.adapter.message.date.DateDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.CompanionMessageDelegate
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.CompanionMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.UserMessageDelegate
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.UserMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.state.MessagesScreenState
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import com.spinoza.messenger_tfs.presentation.ui.*
import com.spinoza.messenger_tfs.presentation.viewmodel.MessagesFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.MessagesFragmentViewModelFactory
import kotlinx.coroutines.launch
import java.util.*

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding: FragmentMessagesBinding
        get() = _binding ?: throw RuntimeException("FragmentMessagesBinding == null")


    private var currentUser: User? = null

    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private lateinit var channelFilter: ChannelFilter

    private val viewModel: MessagesFragmentViewModel by viewModels {
        MessagesFragmentViewModelFactory(
            GetCurrentUserUseCase(MessagesRepositoryImpl.getInstance()),
            GetMessagesUseCase(MessagesRepositoryImpl.getInstance()),
            SendMessageUseCase(MessagesRepositoryImpl.getInstance()),
            UpdateReactionUseCase(MessagesRepositoryImpl.getInstance()),
            channelFilter
        )
    }

    private val messagesAdapter by lazy {
        MessagesAdapter().apply {
            addDelegate(CompanionMessageDelegate())
            addDelegate(UserMessageDelegate())
            addDelegate(DateDelegate())
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

        setupRecyclerView()
        parseParams()
        setupScreen()
    }

    private fun setupScreen() {
        setupOnBackPressedCallback()
        setupStatusBar()
        setupObservers()
        setupListeners()

        binding.textViewTopic.text =
            String.format(getString(R.string.messages_topic_template), channelFilter.topicName)
    }

    private fun setupOnBackPressedCallback() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            onBackPressedCallback
        )
    }

    private fun setupStatusBar() {
        binding.toolbar.title = "#${channelFilter.channel.name}"
        requireActivity().window.statusBarColor =
            requireContext().getThemeColor(R.attr.channel_toolbar_background_color)
    }

    private fun setupRecyclerView() {
        binding.recyclerViewMessages.adapter = messagesAdapter
        binding.recyclerViewMessages.addItemDecoration(StickyDateInHeaderItemDecoration())
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::handleState)
            }
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

    private fun handleState(state: MessagesScreenState) {
        if (state !is MessagesScreenState.Loading) {
            binding.progressBar.off()
        }
        when (state) {
            is MessagesScreenState.Messages -> submitMessages(state.value)
            is MessagesScreenState.UpdateIconImage -> {
                binding.imageViewAction.setImageResource(state.resId)
            }
            is MessagesScreenState.Loading -> binding.progressBar.on()
            is MessagesScreenState.Error -> requireContext().showError(state.value)
            is MessagesScreenState.CurrentUser -> currentUser = state.value
        }
    }

    private fun submitMessages(result: MessagesResult) {
        submitMessages(result.messages) {
            when (result.position.type) {
                MessagePosition.Type.LAST_POSITION -> {
                    val lastItemPosition = messagesAdapter.itemCount - 1
                    binding.recyclerViewMessages.smoothScrollToPosition(lastItemPosition)
                }
                MessagePosition.Type.EXACTLY -> {
                    binding.recyclerViewMessages.smoothScrollToChangedMessage(
                        result.position.messageId
                    )
                }
                MessagePosition.Type.UNDEFINED -> {}
            }
        }
    }

    private fun onAvatarClickListener(messageView: MessageView) {
        App.router.navigateTo(Screens.UserProfile(messageView.userId))
    }

    private fun onReactionAddClickListener(messageView: MessageView) {
        val dialog = ChooseReactionDialogFragment.newInstance(
            messageView.messageId,
        )
        dialog.listener = viewModel::updateReaction
        dialog.show(requireActivity().supportFragmentManager, ChooseReactionDialogFragment.TAG)
    }

    private fun sendMessage() {
        currentUser?.let { user ->
            if (viewModel.sendMessage(user, binding.editTextMessage.text.toString())) {
                binding.editTextMessage.text?.clear()
            }
        }
    }

    private fun submitMessages(messages: List<Message>, callbackAfterSubmit: () -> Unit) {
        currentUser?.let { user ->
            messagesAdapter.submitList(
                messages.groupByDate(
                    user = user,
                    onAvatarClickListener = ::onAvatarClickListener,
                    onReactionAddClickListener = ::onReactionAddClickListener,
                    onReactionClickListener = viewModel::updateReaction
                )
            ) {
                callbackAfterSubmit()
            }
        }
    }

    private fun goBack() {
        App.router.exit()
    }

    @Suppress("deprecation")
    private fun parseParams() {
        val topicName = arguments?.getString(EXTRA_TOPIC_NAME) ?: EMPTY_STRING
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
            channelFilter = ChannelFilter(newChannel, topicName)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadCurrentUser()
        viewModel.loadMessages()
        viewModel.doOnTextChanged(binding.editTextMessage.text)
    }

    override fun onStop() {
        super.onStop()
        onBackPressedCallback.remove()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun List<Message>.groupByDate(
        user: User,
        onAvatarClickListener: ((MessageView) -> Unit)? = null,
        onReactionAddClickListener: ((MessageView) -> Unit)? = null,
        onReactionClickListener: ((MessageView, ReactionView) -> Unit)? = null,
    ): List<DelegateItem> {

        val delegateItemList = mutableListOf<DelegateItem>()
        val dates = TreeSet<MessageDate>()
        this.forEach {
            dates.add(it.date)
        }

        dates.forEach { messageDate ->
            delegateItemList.add(DateDelegateItem(messageDate))
            val allDayMessages = this.filter { message ->
                message.date.date == messageDate.date
            }

            allDayMessages.forEach { message ->
                if (message.user.userId == user.userId) {
                    delegateItemList.add(
                        UserMessageDelegateItem(
                            message,
                            onAvatarClickListener,
                            onReactionAddClickListener,
                            onReactionClickListener
                        )
                    )
                } else {
                    delegateItemList.add(
                        CompanionMessageDelegateItem(
                            message,
                            onAvatarClickListener,
                            onReactionAddClickListener,
                            onReactionClickListener
                        )
                    )
                }
            }
        }

        return delegateItemList
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