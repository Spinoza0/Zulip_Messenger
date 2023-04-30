package com.spinoza.messenger_tfs.presentation.feature.messages

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.FragmentMessagesBinding
import com.spinoza.messenger_tfs.di.messages.DaggerMessagesComponent
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.util.WebUtil
import com.spinoza.messenger_tfs.presentation.adapter.MainDelegateAdapter
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.StickyDateInHeaderItemDecoration
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.date.DateDelegate
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages.OwnMessageDelegate
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages.OwnMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages.UserMessageDelegate
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages.UserMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesResultDelegate
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenEffect
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenState
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.MessageView
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.ReactionView
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.smoothScrollToLastPosition
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.smoothScrollToMessage
import com.spinoza.messenger_tfs.presentation.util.getAppComponent
import com.spinoza.messenger_tfs.presentation.util.getInstanceState
import com.spinoza.messenger_tfs.presentation.util.getParam
import com.spinoza.messenger_tfs.presentation.util.getThemeColor
import com.spinoza.messenger_tfs.presentation.util.off
import com.spinoza.messenger_tfs.presentation.util.on
import com.spinoza.messenger_tfs.presentation.util.restoreInstanceState
import com.spinoza.messenger_tfs.presentation.util.showCheckInternetConnectionDialog
import com.spinoza.messenger_tfs.presentation.util.showError
import vivid.money.elmslie.android.base.ElmFragment
import vivid.money.elmslie.android.storeholder.LifecycleAwareStoreHolder
import vivid.money.elmslie.android.storeholder.StoreHolder
import vivid.money.elmslie.coroutines.ElmStoreCompat
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
    lateinit var webUtil: WebUtil

    @Inject
    lateinit var messagesAdapter: MainDelegateAdapter

    private var _binding: FragmentMessagesBinding? = null
    private val binding: FragmentMessagesBinding
        get() = _binding ?: throw RuntimeException("FragmentMessagesBinding == null")

    private var messagesFilter = MessagesFilter()
    private var onBackPressedCallback: OnBackPressedCallback? = null
    private var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var recyclerViewState: Parcelable? = null
    private var recyclerViewStateOnDestroy: Parcelable? = null

    override val storeHolder:
            StoreHolder<MessagesScreenEvent, MessagesScreenEffect, MessagesScreenState> by lazy {
        LifecycleAwareStoreHolder(lifecycle) { messagesStore }
    }

    override val initEvent: MessagesScreenEvent
        get() = MessagesScreenEvent.Ui.Init

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerMessagesComponent.factory().create(context.getAppComponent(), lifecycle).inject(this)
        pickMedia = registerForActivityResult(PickVisualMedia()) { handlePickMediaResult(it) }
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
        }.also { requireActivity().onBackPressedDispatcher.addCallback(this, it) }
    }

    private fun setupStatusBar() {
        binding.toolbar.title =
            String.format(getString(R.string.channel_name_template, messagesFilter.channel.name))
        requireActivity().window.statusBarColor =
            requireContext().getThemeColor(R.attr.channel_toolbar_background_color)
    }

    private fun setupRecyclerView() {
        messagesAdapter.addDelegate(
            UserMessageDelegate(
                ::onMessageLongClickListener,
                ::onReactionAddClickListener,
                ::onReactionClickListener,
                ::onAvatarClickListener,
            )
        )
        messagesAdapter.addDelegate(
            OwnMessageDelegate(
                ::onMessageLongClickListener,
                ::onReactionAddClickListener,
                ::onReactionClickListener
            )
        )
        messagesAdapter.addDelegate(DateDelegate())
        binding.recyclerViewMessages.adapter = messagesAdapter
        binding.recyclerViewMessages.addItemDecoration(StickyDateInHeaderItemDecoration())
        binding.recyclerViewMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                var firstVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                if (firstVisiblePosition == RecyclerView.NO_POSITION) {
                    firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                }
                var lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
                if (lastVisiblePosition == RecyclerView.NO_POSITION) {
                    lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                }
                store.accept(
                    MessagesScreenEvent.Ui.MessagesOnScrolled(
                        recyclerView.canScrollVertically(MessagesScreenEvent.DIRECTION_UP),
                        recyclerView.canScrollVertically(MessagesScreenEvent.DIRECTION_DOWN),
                        getVisibleMessagesIds(firstVisiblePosition, lastVisiblePosition),
                        firstVisiblePosition, lastVisiblePosition, messagesAdapter.itemCount, dy,
                        isNextMessageExisting(lastVisiblePosition),
                        isLastMessageVisible(lastVisiblePosition)
                    )
                )
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
            imageViewAction.setOnLongClickListener {
                addAttachment()
                true
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
                messagesAdapter.submitList(it.messages) {
                    scrollAfterSubmitMessages(it)
                }
            }
            progressBarLoadingPage.isVisible = state.isLongOperation
            imageViewAction.setImageResource(state.iconActionResId)
            imageViewArrow.isVisible = state.isNextMessageExisting || state.isNewMessageExisting
        }
    }

    override fun handleEffect(effect: MessagesScreenEffect) {
        when (effect) {
            is MessagesScreenEffect.MessageSent -> binding.editTextMessage.text?.clear()
            is MessagesScreenEffect.ScrollToLastMessage ->
                binding.recyclerViewMessages.smoothScrollToLastPosition()

            is MessagesScreenEffect.ShowMessageMenu -> showMessageMenu(effect)
            is MessagesScreenEffect.ShowChooseReactionDialog -> {
                val dialog = ChooseReactionDialogFragment.newInstance(
                    effect.messageId,
                )
                dialog.listener = ::updateReaction
                dialog.show(
                    requireActivity().supportFragmentManager, ChooseReactionDialogFragment.TAG
                )
            }

            is MessagesScreenEffect.Failure.ErrorMessages ->
                showError("${getString(R.string.error_messages)} ${effect.value}")

            is MessagesScreenEffect.Failure.ErrorNetwork -> {
                showError("${getString(R.string.error_network)} ${effect.value}")
                showCheckInternetConnectionDialog({ store.accept(MessagesScreenEvent.Ui.Reload) }) {
                    goBack()
                }
            }

            is MessagesScreenEffect.AddAttachment -> addAttachment()
            is MessagesScreenEffect.FileUploaded ->
                binding.editTextMessage.setText(effect.newMessageText)
        }
    }

    private fun showMessageMenu(effect: MessagesScreenEffect.ShowMessageMenu) {
        val popupMenu = PopupMenu(requireContext(), binding.textViewTopic)
        popupMenu.inflate(R.menu.menu_long_click_on_message)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.itemAddReaction -> {
                    onReactionAddClickListener(effect.messageView)
                    true
                }

                R.id.itemSaveAttachments -> {
                    store.accept(MessagesScreenEvent.Ui.SaveAttachments(effect.urls))
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    private fun getVisibleMessagesIds(
        firstVisiblePosition: Int,
        lastVisiblePosition: Int,
    ): List<Long> {
        val visibleMessageIds = mutableListOf<Long>()
        if (firstVisiblePosition != RecyclerView.NO_POSITION &&
            lastVisiblePosition != RecyclerView.NO_POSITION
        ) {
            for (i in firstVisiblePosition..lastVisiblePosition) {
                val item = messagesAdapter.getItem(i)
                if (item is UserMessageDelegateItem || item is OwnMessageDelegateItem) {
                    visibleMessageIds.add((item.content() as Message).id)
                }
            }
        }
        return visibleMessageIds.toList()
    }

    private fun isNextMessageExisting(lastVisibleItemPosition: Int): Boolean {
        return lastVisibleItemPosition < messagesAdapter.itemCount.minus(LAST_ITEM_OFFSET)
    }

    private fun isLastMessageVisible(lastVisibleItemPosition: Int): Boolean {
        return lastVisibleItemPosition == messagesAdapter.itemCount.minus(LAST_ITEM_OFFSET)
    }

    private fun addAttachment() {
        pickMedia?.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
    }

    private fun handlePickMediaResult(uri: Uri?) {
        if (uri != null) {
            store.accept(MessagesScreenEvent.Ui.UploadFile(binding.editTextMessage.text, uri))
        }
    }

    private fun scrollAfterSubmitMessages(result: MessagesResultDelegate) {
        if (recyclerViewState != null) {
            binding.recyclerViewMessages.restoreInstanceState(recyclerViewState)
            recyclerViewState = null
        } else when (result.position.type) {
            MessagePosition.Type.LAST_POSITION ->
                binding.recyclerViewMessages.smoothScrollToLastPosition()

            MessagePosition.Type.EXACTLY -> {
                binding.recyclerViewMessages.smoothScrollToMessage(result.position.messageId)
            }

            MessagePosition.Type.UNDEFINED -> {}
        }
        val lastVisibleItemPosition =
            (binding.recyclerViewMessages.layoutManager as LinearLayoutManager)
                .findLastVisibleItemPosition()
        store.accept(
            MessagesScreenEvent.Ui.AfterSubmitMessages(
                isNextMessageExisting(lastVisibleItemPosition),
                isLastMessageVisible(lastVisibleItemPosition)
            )
        )
    }

    private fun onAvatarClickListener(messageView: MessageView) {
        store.accept(MessagesScreenEvent.Ui.ShowUserInfo(messageView))
    }

    private fun onMessageLongClickListener(messageView: MessageView) {
        store.accept(MessagesScreenEvent.Ui.OnMessageLongClick(messageView))
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
            recyclerViewState = it.getParam<Parcelable>(PARAM_RECYCLERVIEW_STATE)
        }
    }

    private fun isMessagesListEmpty(): Boolean {
        return messagesAdapter.itemCount == NO_ITEMS
    }

    override fun onStart() {
        super.onStart()
        setupOnBackPressedCallback()
    }

    override fun onResume() {
        super.onResume()
        if (isMessagesListEmpty()) {
            store.accept(MessagesScreenEvent.Ui.Load(messagesFilter))
        }
    }

    override fun onPause() {
        super.onPause()
        binding.shimmerLarge.off()
        binding.shimmerSending.off()
    }

    override fun onStop() {
        super.onStop()
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        recyclerViewState = if (_binding != null) {
            binding.recyclerViewMessages.getInstanceState()
        } else {
            recyclerViewStateOnDestroy
        }
        outState.putParcelable(PARAM_RECYCLERVIEW_STATE, recyclerViewState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerViewStateOnDestroy = binding.recyclerViewMessages.getInstanceState()
        messagesAdapter.clear()
        _binding = null
    }

    companion object {

        private const val PARAM_CHANNEL_FILTER = "messagesFilter"
        private const val PARAM_RECYCLERVIEW_STATE = "recyclerViewState"
        private const val NO_ITEMS = 0
        private const val LAST_ITEM_OFFSET = 1

        fun newInstance(messagesFilter: MessagesFilter): MessagesFragment {
            return MessagesFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PARAM_CHANNEL_FILTER, messagesFilter)
                }
            }
        }
    }
}