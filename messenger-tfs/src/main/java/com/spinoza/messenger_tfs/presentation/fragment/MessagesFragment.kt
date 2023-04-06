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
import com.spinoza.messenger_tfs.presentation.model.MessagesResultDelegate
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import com.spinoza.messenger_tfs.presentation.state.MessagesScreenState
import com.spinoza.messenger_tfs.presentation.ui.*
import com.spinoza.messenger_tfs.presentation.viewmodel.MessagesFragmentViewModel
import com.spinoza.messenger_tfs.presentation.viewmodel.factory.MessagesFragmentViewModelFactory
import kotlinx.coroutines.launch
import java.util.*

class MessagesFragment : Fragment() {

    private val globalRouter = App.router

    private var _binding: FragmentMessagesBinding? = null
    private val binding: FragmentMessagesBinding
        get() = _binding ?: throw RuntimeException("FragmentMessagesBinding == null")

    private lateinit var messagesFilter: MessagesFilter
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private var isGoingBack = false

    private val viewModel: MessagesFragmentViewModel by viewModels {
        MessagesFragmentViewModelFactory(
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
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            onBackPressedCallback
        )
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
                    ::onReactionAddClickListener,
                    ::onReactionClickListener,
                    ::onAvatarClickListener
                )
            )
            addDelegate(
                OwnMessageDelegate(::onReactionAddClickListener, ::onReactionClickListener)
            )
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
                for (i in firstVisiblePosition..lastVisiblePosition) {
                    val item = adapter.getItem(i)
                    if (item is UserMessageDelegateItem || item is OwnMessageDelegateItem) {
                        viewModel.addToReadMessageIds((item.content() as Message).id)
                    }
                }
            }
        })
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
            viewModel.sendMessage(binding.editTextMessage.text.toString())
        }
        binding.editTextMessage.doOnTextChanged { text, _, _, _ ->
            viewModel.doOnTextChanged(text)
        }
        binding.imageViewArrow.setOnClickListener {
            binding.recyclerViewMessages.smoothScrollToLastPosition()
        }
    }

    private fun handleState(state: MessagesScreenState) {
        if (state !is MessagesScreenState.Loading) {
            binding.shimmerLarge.off()
        }
        if (state !is MessagesScreenState.SendingMessage) {
            binding.shimmerSending.off()
        }
        when (state) {
            is MessagesScreenState.Messages -> {
                submitMessages(state.value)
            }
            is MessagesScreenState.UpdateIconImage -> {
                binding.imageViewAction.setImageResource(state.resId)
            }
            is MessagesScreenState.MessageSent -> {
                binding.editTextMessage.text?.clear()
            }
            is MessagesScreenState.ReactionSent -> {}
            is MessagesScreenState.Loading -> {
                if (messagesListIsEmpty()) binding.shimmerLarge.on()
            }
            is MessagesScreenState.Failure -> {
                handleErrors(state)
            }
            is MessagesScreenState.SendingMessage -> binding.shimmerSending.on()
        }
    }

    private fun handleErrors(error: MessagesScreenState.Failure) {
        when (error) {
            is MessagesScreenState.Failure.MessageNotFound -> showError(
                String.format(getString(R.string.error_message_not_found), error.messageId)
            )
            is MessagesScreenState.Failure.UserNotFound -> showError(
                String.format(getString(R.string.error_user_not_found), error.userId)
            )
            is MessagesScreenState.Failure.SendingMessage -> showError(
                String.format(getString(R.string.error_sending_message), error.value)
            )
            is MessagesScreenState.Failure.UpdatingReaction -> showError(
                String.format(getString(R.string.error_updating_reaction), error.value)
            )
            is MessagesScreenState.Failure.OwnUserNotFound -> {
                showError(String.format(getString(R.string.error_loading_user), error.value))
                goBack()
            }
            is MessagesScreenState.Failure.Network -> {
                showError(String.format(getString(R.string.error_network), error.value))
                showCheckInternetConnectionDialog(viewModel::loadMessages) { goBack() }
            }
            is MessagesScreenState.Failure.LoadingMessages -> showError(
                String.format(
                    getString(R.string.error_loading_messages),
                    error.messagesFilter.channel.name,
                    error.messagesFilter.topic.name,
                    error.value
                )
            )
        }
    }

    private fun submitMessages(result: MessagesResultDelegate) {
        val messagesAdapter =
            binding.recyclerViewMessages.adapter as MainDelegateAdapter
        messagesAdapter.submitList(result.messages) {
            when (result.position.type) {
                MessagePosition.Type.LAST_POSITION ->
                    binding.recyclerViewMessages.smoothScrollToLastPosition()
                MessagePosition.Type.EXACTLY -> {
                    binding.recyclerViewMessages.smoothScrollToMessage(
                        result.position.messageId
                    )
                }
                MessagePosition.Type.UNDEFINED -> {}
            }
            showArrowDown()
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
        globalRouter.navigateTo(Screens.UserProfile(messageView.userId))
    }

    private fun onReactionAddClickListener(messageView: MessageView) {
        val dialog = ChooseReactionDialogFragment.newInstance(
            messageView.messageId,
        )
        dialog.listener = viewModel::updateReaction
        dialog.show(requireActivity().supportFragmentManager, ChooseReactionDialogFragment.TAG)
    }

    private fun onReactionClickListener(messageView: MessageView, reactionView: ReactionView) {
        viewModel.updateReaction(messageView.messageId, reactionView.emoji)
    }

    private fun goBack() {
        if (!isGoingBack) {
            isGoingBack = true
            viewModel.setMessageReadFlags()
            globalRouter.exit()
        }
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