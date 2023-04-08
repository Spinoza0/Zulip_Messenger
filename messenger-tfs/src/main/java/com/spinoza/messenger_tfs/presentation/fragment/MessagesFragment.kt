package com.spinoza.messenger_tfs.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.App
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentMessagesBinding
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.adapter.delegate.MainDelegateAdapter
import com.spinoza.messenger_tfs.presentation.adapter.message.StickyDateInHeaderItemDecoration
import com.spinoza.messenger_tfs.presentation.adapter.message.date.DateDelegate
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.OwnMessageDelegate
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.OwnMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.UserMessageDelegate
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.UserMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesEffect
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesEvent
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesResultDelegate
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesState
import com.spinoza.messenger_tfs.presentation.ui.*
import com.spinoza.messenger_tfs.presentation.viewmodel.MessagesFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.MessagesFragmentViewModelFactory
import kotlinx.coroutines.launch
import java.util.*

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding: FragmentMessagesBinding
        get() = _binding ?: throw RuntimeException("FragmentMessagesBinding == null")

    private lateinit var messagesFilter: MessagesFilter
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    private val viewModel: MessagesFragmentViewModel by viewModels {
        MessagesFragmentViewModelFactory(
            App.router,
            messagesFilter,
            GetOwnUserIdUseCase(MessagesRepositoryImpl.getInstance()),
            GetMessagesUseCase(MessagesRepositoryImpl.getInstance()),
            SendMessageUseCase(MessagesRepositoryImpl.getInstance()),
            UpdateReactionUseCase(MessagesRepositoryImpl.getInstance()),
            RegisterEventQueueUseCase(MessagesRepositoryImpl.getInstance()),
            DeleteEventQueueUseCase(MessagesRepositoryImpl.getInstance()),
            GetMessageEventUseCase(MessagesRepositoryImpl.getInstance()),
            GetDeleteMessageEventUseCase(MessagesRepositoryImpl.getInstance()),
            GetReactionEventUseCase(MessagesRepositoryImpl.getInstance()),
            SetOwnStatusActiveUseCase(MessagesRepositoryImpl.getInstance()),
            SetMessagesFlagToReadUserCase(MessagesRepositoryImpl.getInstance()),
        )
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
        setupRecyclerView()
        setupStatusBar()
        setupObservers()
        setupListeners()
        setupScreen()
    }

    private fun setupScreen() {
        binding.textViewTopic.text =
            String.format(getString(R.string.messages_topic_template), messagesFilter.topic.name)
    }

    private fun setupOnBackPressedCallback() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun setupStatusBar() {
        binding.toolbar.title =
            String.format(getString(R.string.channel_name_template, messagesFilter.channel.name))
        requireActivity().window.statusBarColor =
            requireContext().getThemeColor(R.attr.channel_toolbar_background_color)
    }

    private fun setupRecyclerView() {
        val messagesAdapter = MainDelegateAdapter().apply {
            addDelegate(
                UserMessageDelegate(
                    ::onReactionAddClickListener, ::onReactionClickListener, ::onAvatarClickListener
                )
            )
            addDelegate(OwnMessageDelegate(::onReactionAddClickListener, ::onReactionClickListener))
            addDelegate(DateDelegate())
        }
        binding.recyclerViewMessages.adapter = messagesAdapter
        binding.recyclerViewMessages.addItemDecoration(StickyDateInHeaderItemDecoration())
        binding.recyclerViewMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                showArrowDown()
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val adapter = binding.recyclerViewMessages.adapter as MainDelegateAdapter
                val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                val messageIds = mutableListOf<Long>()
                for (i in firstVisiblePosition..lastVisiblePosition) {
                    val item = adapter.getItem(i)
                    if (item is UserMessageDelegateItem || item is OwnMessageDelegateItem) {
                        messageIds.add((item.content() as Message).id)
                    }
                }
                viewModel.reduce(MessagesEvent.Ui.SetMessagesRead(messageIds))
            }
        })
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect(::handleState)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect(::handleEffect)
            }
        }
    }

    private fun setupListeners() {
        with(binding) {
            toolbar.setNavigationOnClickListener {
                goBack()
            }
            imageViewAction.setOnClickListener {
                viewModel.reduce(MessagesEvent.Ui.SendMessage(editTextMessage.text))
            }
            editTextMessage.doOnTextChanged { text, _, _, _ ->
                viewModel.reduce(MessagesEvent.Ui.NewMessageText(text))
            }
            imageViewArrow.setOnClickListener {
                binding.recyclerViewMessages.smoothScrollToLastPosition()
            }
        }
    }

    private fun handleState(state: MessagesState) {
        if (state.isLoading) {
            if (messagesListIsEmpty()) binding.shimmerLarge.on()
        } else {
            binding.shimmerLarge.off()
        }
        if (state.isSendingMessage) {
            binding.shimmerSending.on()
        } else {
            binding.shimmerSending.off()
        }
        state.messages?.let {
            submitMessages(it)
        }
        binding.imageViewAction.setImageResource(state.iconActionResId)
    }

    private fun handleEffect(effect: MessagesEffect) {
        when (effect) {
            is MessagesEffect.MessageSent -> binding.editTextMessage.text?.clear()
            is MessagesEffect.Failure.MessageNotFound -> showError(
                String.format(getString(R.string.error_message_not_found), effect.messageId)
            )
            is MessagesEffect.Failure.UserNotFound -> showError(
                String.format(getString(R.string.error_user_not_found), effect.userId)
            )
            is MessagesEffect.Failure.SendingMessage -> showError(
                String.format(getString(R.string.error_sending_message), effect.value)
            )
            is MessagesEffect.Failure.UpdatingReaction -> showError(
                String.format(getString(R.string.error_updating_reaction), effect.value)
            )
            is MessagesEffect.Failure.OwnUserNotFound -> {
                showError(String.format(getString(R.string.error_loading_user), effect.value))
                goBack()
            }
            is MessagesEffect.Failure.Network -> {
                showError(String.format(getString(R.string.error_network), effect.value))
                showCheckInternetConnectionDialog({ viewModel.reduce(MessagesEvent.Ui.Load) }) {
                    goBack()
                }
            }
            is MessagesEffect.Failure.LoadingMessages -> showError(
                String.format(
                    getString(R.string.error_loading_messages),
                    effect.messagesFilter.channel.name,
                    effect.messagesFilter.topic.name,
                    effect.value
                )
            )
        }
    }

    private fun submitMessages(result: MessagesResultDelegate) {
        with(binding) {
            val messagesAdapter = recyclerViewMessages.adapter as MainDelegateAdapter
            messagesAdapter.submitList(result.messages) {
                when (result.position.type) {
                    MessagePosition.Type.LAST_POSITION ->
                        recyclerViewMessages.smoothScrollToLastPosition()
                    MessagePosition.Type.EXACTLY -> {
                        recyclerViewMessages.smoothScrollToMessage(result.position.messageId)
                    }
                    MessagePosition.Type.UNDEFINED -> {}
                }
                viewModel.reduce(MessagesEvent.Ui.AfterSubmitMessages)
            }
        }
    }

    private fun showArrowDown() {
        val layoutManager = binding.recyclerViewMessages.layoutManager as LinearLayoutManager
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        val lastItemPosition = binding.recyclerViewMessages.adapter?.itemCount?.minus(1)
        binding.imageViewArrow.isVisible =
            lastItemPosition != null && lastVisibleItemPosition < lastItemPosition
    }

    private fun onAvatarClickListener(messageView: MessageView) {
        viewModel.reduce(MessagesEvent.Ui.ShowUserInfo(messageView))
    }

    private fun onReactionAddClickListener(messageView: MessageView) {
        val dialog = ChooseReactionDialogFragment.newInstance(
            messageView.messageId,
        )
        dialog.listener = ::updateReaction
        dialog.show(requireActivity().supportFragmentManager, ChooseReactionDialogFragment.TAG)
    }

    private fun onReactionClickListener(messageView: MessageView, reactionView: ReactionView) {
        updateReaction(messageView.messageId, reactionView.emoji)
    }

    private fun updateReaction(messageId: Long, emoji: Emoji) {
        viewModel.reduce(MessagesEvent.Ui.UpdateReaction(messageId, emoji))
    }

    private fun goBack() {
        viewModel.reduce(MessagesEvent.Ui.Exit)
    }

    @Suppress("deprecation")
    private fun parseParams() {
        val newMessagesFilter = arguments?.getParam<MessagesFilter>(PARAM_CHANNEL_FILTER)
        if (newMessagesFilter == null ||
            newMessagesFilter.channel.channelId == Channel.UNDEFINED_ID ||
            newMessagesFilter.topic.name.isEmpty()
        ) {
            goBack()
        } else {
            messagesFilter = newMessagesFilter
        }
    }

    private fun messagesListIsEmpty(): Boolean {
        return (binding.recyclerViewMessages.adapter as MainDelegateAdapter).itemCount == NO_ITEMS
    }

    override fun onStart() {
        super.onStart()
        setupOnBackPressedCallback()
    }

    override fun onPause() {
        super.onPause()
        binding.shimmerLarge.off()
        binding.shimmerSending.off()
    }

    override fun onStop() {
        super.onStop()
        onBackPressedCallback.remove()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (binding.recyclerViewMessages.adapter as MainDelegateAdapter).clear()
        _binding = null
    }

    companion object {

        private const val PARAM_CHANNEL_FILTER = "messagesFilter"
        private const val NO_ITEMS = 0

        fun newInstance(messagesFilter: MessagesFilter): MessagesFragment {
            return MessagesFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PARAM_CHANNEL_FILTER, messagesFilter)
                }
            }
        }
    }
}