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

    private var _binding: FragmentMessagesBinding? = null
    private val binding: FragmentMessagesBinding
        get() = _binding ?: throw RuntimeException("FragmentMessagesBinding == null")


    private lateinit var channelFilter: ChannelFilter
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    private val viewModel: MessagesFragmentViewModel by viewModels {
        MessagesFragmentViewModelFactory(
            GetCurrentUserUseCase(MessagesRepositoryImpl.getInstance()),
            GetMessagesUseCase(MessagesRepositoryImpl.getInstance()),
            SendMessageUseCase(MessagesRepositoryImpl.getInstance()),
            UpdateReactionUseCase(MessagesRepositoryImpl.getInstance()),
            channelFilter,
            ::onAvatarClickListener,
            ::onReactionAddClickListener
        )
    }

    private val messagesAdapter by lazy {
        MainDelegateAdapter().apply {
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
            String.format(getString(R.string.messages_topic_template), channelFilter.topic.name)
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
        }
    }

    private fun submitMessages(result: MessagesResultDelegate) {
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
        if (viewModel.sendMessage(binding.editTextMessage.text.toString())) {
            binding.editTextMessage.text?.clear()
        }
    }

    private fun goBack() {
        App.router.exit()
    }

    @Suppress("deprecation")
    private fun parseParams() {
        val newChannelFilter = arguments?.getParam<ChannelFilter>(PARAM_CHANNEL_FILTER)
        if (newChannelFilter == null ||
            newChannelFilter.channel.channelId == Channel.UNDEFINED_ID ||
            newChannelFilter.topic.name.isEmpty()
        ) {
            goBack()
        } else {
            channelFilter = newChannelFilter
        }
    }

    override fun onResume() {
        super.onResume()

        if (binding.recyclerViewMessages.adapter?.itemCount == NO_MESSAGES) {
            viewModel.loadMessages()
        }
    }

    override fun onStop() {
        super.onStop()
        onBackPressedCallback.remove()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        private const val PARAM_CHANNEL_FILTER = "channelFilter"
        private const val NO_MESSAGES = 0

        fun newInstance(channelFilter: ChannelFilter): MessagesFragment {
            return MessagesFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PARAM_CHANNEL_FILTER, channelFilter)
                }
            }
        }
    }
}