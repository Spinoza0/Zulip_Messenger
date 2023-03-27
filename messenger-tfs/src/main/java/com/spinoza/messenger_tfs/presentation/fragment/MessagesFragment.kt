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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.spinoza.messenger_tfs.App
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.repository.MessagesRepositoryImpl
import com.spinoza.messenger_tfs.databinding.FragmentMessagesBinding
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.repository.MessagePosition
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.adapter.delegate.MainDelegateAdapter
import com.spinoza.messenger_tfs.presentation.adapter.message.StickyDateInHeaderItemDecoration
import com.spinoza.messenger_tfs.presentation.adapter.message.date.DateDelegate
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.CompanionMessageDelegate
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.UserMessageDelegate
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
            GetCurrentUserUseCase(MessagesRepositoryImpl.getInstance()),
            GetMessagesUseCase(MessagesRepositoryImpl.getInstance()),
            SendMessageUseCase(MessagesRepositoryImpl.getInstance()),
            UpdateReactionUseCase(MessagesRepositoryImpl.getInstance()),
            messagesFilter
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
        setupOnBackPressedCallback()
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
                CompanionMessageDelegate(
                    ::onReactionAddClickListener,
                    ::onReactionClickListener,
                    ::onAvatarClickListener
                )
            )
            addDelegate(
                UserMessageDelegate(
                    ::onReactionAddClickListener,
                    ::onReactionClickListener
                )
            )
            addDelegate(DateDelegate())
        }

        binding.recyclerViewMessages.adapter = messagesAdapter
        binding.recyclerViewMessages.addItemDecoration(StickyDateInHeaderItemDecoration())
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
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
    }

    private fun handleState(state: MessagesScreenState) {
        if (state !is MessagesScreenState.Loading) {
            binding.shimmerLarge.off()
            binding.shimmerSmall.off()
        }
        when (state) {
            is MessagesScreenState.Messages -> {
                binding.shimmerSent.off()
                submitMessages(state.value)
            }
            is MessagesScreenState.UpdateIconImage -> {
                binding.imageViewAction.setImageResource(state.resId)
            }
            is MessagesScreenState.MessageSent -> {
                binding.editTextMessage.text?.clear()
                binding.shimmerSent.on()
            }
            is MessagesScreenState.ReactionSent -> binding.shimmerSent.on()
            is MessagesScreenState.Loading -> {
                binding.shimmerSent.off()
                if (messagesListIsEmpty()) binding.shimmerLarge.on()
                else binding.shimmerSmall.on()
            }
            is MessagesScreenState.Failure -> {
                binding.shimmerSent.off()
                handleErrors(state)
            }
        }
    }

    private fun handleErrors(error: MessagesScreenState.Failure) {
        when (error) {
            is MessagesScreenState.Failure.MessageNotFound -> showError(
                String.format(
                    getString(R.string.error_message_not_found),
                    error.messageId
                )
            )
            is MessagesScreenState.Failure.UserNotFound -> showError(
                String.format(
                    getString(R.string.error_user_not_found),
                    error.userId
                )
            )
            is MessagesScreenState.Failure.SendingMessage -> showError(
                String.format(
                    getString(R.string.error_sending_message),
                    error.value
                )
            )
            is MessagesScreenState.Failure.UpdatingReaction -> showError(
                String.format(
                    getString(R.string.error_updating_reaction),
                    error.value
                )
            )
            is MessagesScreenState.Failure.CurrentUserNotFound -> {
                showError(
                    String.format(
                        getString(R.string.error_loading_current_user),
                        error.value
                    )
                )
                goBack()
            }
        }
    }

    private fun submitMessages(result: MessagesResultDelegate) {
        val messagesAdapter =
            binding.recyclerViewMessages.adapter as MainDelegateAdapter
        messagesAdapter.submitList(result.messages) {
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

    override fun onResume() {
        super.onResume()
        viewModel.onResume(messagesListIsEmpty())
    }

    override fun onPause() {
        super.onPause()
        binding.shimmerLarge.off()
        binding.shimmerSmall.off()
        binding.shimmerSent.off()
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