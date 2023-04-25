package com.spinoza.messenger_tfs.presentation.feature.messages

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.FragmentMessagesBinding
import com.spinoza.messenger_tfs.di.messages.DaggerMessagesComponent
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.feature.app.adapter.MainDelegateAdapter
import com.spinoza.messenger_tfs.presentation.feature.app.utils.getAppComponent
import com.spinoza.messenger_tfs.presentation.feature.app.utils.getParam
import com.spinoza.messenger_tfs.presentation.feature.app.utils.showCheckInternetConnectionDialog
import com.spinoza.messenger_tfs.presentation.feature.app.utils.showError
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.StickyDateInHeaderItemDecoration
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.date.DateDelegate
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages.OwnMessageDelegate
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages.UserMessageDelegate
import com.spinoza.messenger_tfs.presentation.feature.messages.model.*
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.*
import vivid.money.elmslie.android.base.ElmFragment
import vivid.money.elmslie.android.storeholder.LifecycleAwareStoreHolder
import vivid.money.elmslie.android.storeholder.StoreHolder
import vivid.money.elmslie.coroutines.ElmStoreCompat
import java.util.*
import javax.inject.Inject

class MessagesFragment :
    ElmFragment<MessagesScreenEvent, MessagesScreenEffect, MessagesScreenState>() {

    @Inject
    lateinit var messagesStore: ElmStoreCompat<
            MessagesScreenEvent,
            MessagesScreenState,
            MessagesScreenEffect,
            MessagesScreenCommand>

    @Inject
    lateinit var appAuthKeeper: AppAuthKeeper

    private var _binding: FragmentMessagesBinding? = null
    private val binding: FragmentMessagesBinding
        get() = _binding ?: throw RuntimeException("FragmentMessagesBinding == null")

    private lateinit var messagesFilter: MessagesFilter
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private var recyclerViewState: Parcelable? = null

    override val storeHolder:
            StoreHolder<MessagesScreenEvent, MessagesScreenEffect, MessagesScreenState> by lazy {
        LifecycleAwareStoreHolder(lifecycle) { messagesStore }
    }

    override val initEvent: MessagesScreenEvent
        get() = MessagesScreenEvent.Ui.Init

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerMessagesComponent.factory().create(context.getAppComponent(), lifecycle).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parseParams(savedInstanceState)
        setupRecyclerView()
        setupStatusBar()
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
                    ::onReactionAddClickListener,
                    ::onReactionClickListener,
                    ::onAvatarClickListener,
                    appAuthKeeper.data
                )
            )
            addDelegate(
                OwnMessageDelegate(
                    ::onReactionAddClickListener,
                    ::onReactionClickListener,
                    appAuthKeeper.data
                )
            )
            addDelegate(DateDelegate())
        }
        binding.recyclerViewMessages.adapter = messagesAdapter
        binding.recyclerViewMessages.addItemDecoration(StickyDateInHeaderItemDecoration())
        binding.recyclerViewMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                store.accept(MessagesScreenEvent.Ui.MessagesOnScrolled(dy))
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    store.accept(MessagesScreenEvent.Ui.MessagesScrollStateIdle)
            }
        })
    }

    private fun setupListeners() {
        with(binding) {
            toolbar.setNavigationOnClickListener {
                goBack()
            }
            imageViewAction.setOnClickListener {
                store.accept(MessagesScreenEvent.Ui.SendMessage(editTextMessage.text))
            }
            editTextMessage.doOnTextChanged { text, _, _, _ ->
                store.accept(MessagesScreenEvent.Ui.NewMessageText(text))
            }
            imageViewArrow.setOnClickListener {
                store.accept(MessagesScreenEvent.Ui.ScrollToLastMessage)
            }
        }
    }

    override fun render(state: MessagesScreenState) {
        with(binding) {
            if (state.isLoading && isMessagesListEmpty()) {
                shimmerLarge.on()
            } else {
                shimmerLarge.off()
            }
            if (state.isSendingMessage) {
                shimmerSending.on()
            } else {
                shimmerSending.off()
            }
            state.messages?.let {
                (recyclerViewMessages.adapter as MainDelegateAdapter).submitList(it.messages) {
                    scrollAfterSubmitMessages(it)
                }

            }
            progressBarLoadingPage.isVisible =
                state.isLoadingPreviousPage || state.isLoadingNextPage
            imageViewAction.setImageResource(state.iconActionResId)
            imageViewArrow.isVisible = state.isNextMessageExisting || state.isNewMessageExisting
        }
    }

    override fun handleEffect(effect: MessagesScreenEffect) {
        when (effect) {
            is MessagesScreenEffect.MessageSent -> binding.editTextMessage.text?.clear()
            is MessagesScreenEffect.ScrollToLastMessage ->
                binding.recyclerViewMessages.smoothScrollToLastPosition()
            is MessagesScreenEffect.ShowChooseReactionDialog -> {
                val dialog = ChooseReactionDialogFragment.newInstance(
                    effect.messageId,
                )
                dialog.listener = ::updateReaction
                dialog.show(
                    requireActivity().supportFragmentManager, ChooseReactionDialogFragment.TAG
                )
            }
            is MessagesScreenEffect.Failure.ErrorMessages -> showError(
                String.format(getString(R.string.error_messages), effect.value)
            )
            is MessagesScreenEffect.Failure.ErrorNetwork -> {
                showError(String.format(getString(R.string.error_network), effect.value))
                showCheckInternetConnectionDialog({ store.accept(MessagesScreenEvent.Ui.Reload) }) {
                    goBack()
                }
            }
        }
    }

    private fun scrollAfterSubmitMessages(result: MessagesResultDelegate) {
        if (recyclerViewState != null) {
            binding.recyclerViewMessages.layoutManager?.onRestoreInstanceState(recyclerViewState)
            recyclerViewState = null
        } else when (result.position.type) {
            MessagePosition.Type.LAST_POSITION ->
                binding.recyclerViewMessages.smoothScrollToLastPosition()
            MessagePosition.Type.EXACTLY -> {
                binding.recyclerViewMessages.smoothScrollToMessage(result.position.messageId)
            }
            MessagePosition.Type.UNDEFINED -> {}
        }
        store.accept(MessagesScreenEvent.Ui.AfterSubmitMessages)
    }

    private fun onAvatarClickListener(messageView: MessageView) {
        store.accept(MessagesScreenEvent.Ui.ShowUserInfo(messageView))
    }

    private fun onReactionAddClickListener(messageView: MessageView) {
        store.accept(MessagesScreenEvent.Ui.ShowChooseReactionDialog(messageView))
    }

    private fun onReactionClickListener(messageView: MessageView, reactionView: ReactionView) {
        updateReaction(messageView.messageId, reactionView.emoji)
    }

    private fun updateReaction(messageId: Long, emoji: Emoji) {
        store.accept(MessagesScreenEvent.Ui.UpdateReaction(messageId, emoji))
    }

    private fun goBack() {
        store.accept(MessagesScreenEvent.Ui.Exit)
    }

    @Suppress("deprecation")
    private fun parseParams(savedInstanceState: Bundle?) {
        val newMessagesFilter = arguments?.getParam<MessagesFilter>(PARAM_CHANNEL_FILTER)
        if (newMessagesFilter == null ||
            newMessagesFilter.channel.channelId == Channel.UNDEFINED_ID ||
            newMessagesFilter.topic.name.isEmpty()
        ) {
            goBack()
        } else {
            messagesFilter = newMessagesFilter
        }
        savedInstanceState?.let {
            recyclerViewState = it.getParcelable(PARAM_RECYCLERVIEW_STATE)
        }
    }

    private fun isMessagesListEmpty(): Boolean {
        return (binding.recyclerViewMessages.adapter as MainDelegateAdapter).itemCount == NO_ITEMS
    }

    override fun onStart() {
        super.onStart()
        setupOnBackPressedCallback()
    }

    override fun onResume() {
        super.onResume()
        if (isMessagesListEmpty()) {
            store.accept(MessagesScreenEvent.Ui.Load(binding.recyclerViewMessages, messagesFilter))
        }
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        recyclerViewState = binding.recyclerViewMessages.layoutManager?.onSaveInstanceState()
        outState.putParcelable(PARAM_RECYCLERVIEW_STATE, recyclerViewState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (binding.recyclerViewMessages.adapter as MainDelegateAdapter).clear()
        _binding = null
    }

    companion object {

        private const val PARAM_CHANNEL_FILTER = "messagesFilter"
        private const val PARAM_RECYCLERVIEW_STATE = "recyclerViewState"
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