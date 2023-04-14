package com.spinoza.messenger_tfs.presentation.fragment

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyberfox21.tinkofffintechseminar.di.GlobalDI
import com.spinoza.messenger_tfs.R
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
import com.spinoza.messenger_tfs.presentation.elmstore.MessagesActor
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesResultDelegate
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesScreenEffect
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesScreenEvent
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesScreenState
import com.spinoza.messenger_tfs.presentation.ui.*
import vivid.money.elmslie.android.base.ElmFragment
import vivid.money.elmslie.android.storeholder.LifecycleAwareStoreHolder
import vivid.money.elmslie.android.storeholder.StoreHolder
import java.util.*

class MessagesFragment :
    ElmFragment<MessagesScreenEvent, MessagesScreenEffect, MessagesScreenState>() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding: FragmentMessagesBinding
        get() = _binding ?: throw RuntimeException("FragmentMessagesBinding == null")

    private lateinit var messagesFilter: MessagesFilter
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private var recyclerViewState: Parcelable? = null

    override val storeHolder:
            StoreHolder<MessagesScreenEvent, MessagesScreenEffect, MessagesScreenState> by lazy {
        LifecycleAwareStoreHolder(lifecycle) {
            GlobalDI.INSTANCE.provideMessagesStore(MessagesActor(lifecycle))
        }
    }

    override val initEvent: MessagesScreenEvent
        get() = MessagesScreenEvent.Ui.Init

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
                store.accept(MessagesScreenEvent.Ui.VisibleMessages(getVisibleMessagesIds()))
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
                binding.recyclerViewMessages.smoothScrollToLastPosition()
            }
        }
    }

    override fun render(state: MessagesScreenState) {
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
            (binding.recyclerViewMessages.adapter as MainDelegateAdapter).submitList(it.messages) {
                scrollAfterSubmitMessages(it)
            }

        }
        binding.imageViewAction.setImageResource(state.iconActionResId)
    }

    override fun handleEffect(effect: MessagesScreenEffect) {
        when (effect) {
            is MessagesScreenEffect.MessageSent -> binding.editTextMessage.text?.clear()
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
                showCheckInternetConnectionDialog({
                    store.accept(MessagesScreenEvent.Ui.Load(messagesFilter))
                }) {
                    goBack()
                }
            }
        }
    }

    private fun getVisibleMessagesIds(): List<Long> {
        val layoutManager = binding.recyclerViewMessages.layoutManager as LinearLayoutManager
        val adapter = binding.recyclerViewMessages.adapter as MainDelegateAdapter
        var firstVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
        if (firstVisiblePosition == UNDEFINED_POSITION) {
            firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        }
        var lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
        if (lastVisiblePosition == UNDEFINED_POSITION) {
            lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
        }
        val messageIds = mutableListOf<Long>()
        if (firstVisiblePosition != UNDEFINED_POSITION && lastVisiblePosition != UNDEFINED_POSITION)
            for (i in firstVisiblePosition..lastVisiblePosition) {
                val item = adapter.getItem(i)
                if (item is UserMessageDelegateItem || item is OwnMessageDelegateItem) {
                    messageIds.add((item.content() as Message).id)
                }
            }
        return messageIds
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
        showArrowDown()
        store.accept(MessagesScreenEvent.Ui.AfterSubmitMessages)
    }

    private fun showArrowDown() {
        val layoutManager = binding.recyclerViewMessages.layoutManager as LinearLayoutManager
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        val lastItemPosition = binding.recyclerViewMessages.adapter?.itemCount?.minus(1)
        binding.imageViewArrow.isVisible =
            lastItemPosition != null && lastVisibleItemPosition < lastItemPosition
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

    private fun messagesListIsEmpty(): Boolean {
        return (binding.recyclerViewMessages.adapter as MainDelegateAdapter).itemCount == NO_ITEMS
    }

    override fun onStart() {
        super.onStart()
        setupOnBackPressedCallback()
    }

    override fun onResume() {
        super.onResume()
        if (messagesListIsEmpty()) {
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
        private const val UNDEFINED_POSITION = -1

        fun newInstance(messagesFilter: MessagesFilter): MessagesFragment {
            return MessagesFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PARAM_CHANNEL_FILTER, messagesFilter)
                }
            }
        }
    }
}