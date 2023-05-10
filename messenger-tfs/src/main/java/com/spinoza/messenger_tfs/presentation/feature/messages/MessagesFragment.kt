package com.spinoza.messenger_tfs.presentation.feature.messages

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.BuildConfig
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.FragmentMessagesBinding
import com.spinoza.messenger_tfs.di.messages.DaggerMessagesComponent
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.util.EMPTY_STRING
import com.spinoza.messenger_tfs.presentation.adapter.MainDelegateAdapter
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.StickyDateInHeaderItemDecoration
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.date.DateDelegate
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages.OwnMessageDelegate
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages.OwnMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages.UserMessageDelegate
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages.UserMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.topic.MessagesTopicDelegate
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesResultDelegate
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenEffect
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenEvent
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenState
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.MessageView
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.ReactionView
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.smoothScrollToLastPosition
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.smoothScrollToMessage
import com.spinoza.messenger_tfs.presentation.notification.Notificator
import com.spinoza.messenger_tfs.presentation.util.DIRECTION_DOWN
import com.spinoza.messenger_tfs.presentation.util.DIRECTION_UP
import com.spinoza.messenger_tfs.presentation.util.ExternalStoragePermission
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
    lateinit var messagesAdapter: MainDelegateAdapter

    @Inject
    lateinit var notificator: Notificator

    @Inject
    lateinit var externalStoragePermission: ExternalStoragePermission

    private var _binding: FragmentMessagesBinding? = null
    private val binding: FragmentMessagesBinding
        get() = _binding ?: throw RuntimeException("FragmentMessagesBinding == null")

    private var messagesFilter = MessagesFilter()
    private var onBackPressedCallback: OnBackPressedCallback? = null
    private var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var recyclerViewState: Parcelable? = null
    private var recyclerViewStateOnDestroy: Parcelable? = null
    private val topicNameTemplate by lazy { getString(R.string.messages_topic_template) }

    override val storeHolder:
            StoreHolder<MessagesScreenEvent, MessagesScreenEffect, MessagesScreenState> by lazy {
        LifecycleAwareStoreHolder(lifecycle) { messagesStore }
    }

    override val initEvent: MessagesScreenEvent
        get() = MessagesScreenEvent.Ui.Init

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerMessagesComponent.factory().create(context.getAppComponent(), this).inject(this)
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
        if (savedInstanceState != null) {
            store.accept(MessagesScreenEvent.Ui.CheckLoginStatus)
        }
        setupRecyclerView()
        setupStatusBar()
        setupListeners()
    }

    private fun setupTopicTitle() {
        with(binding) {
            textViewTopic.isVisible = messagesFilter.topic.name.isNotEmpty()
            imageViewTopicArrow.isVisible = messagesFilter.topic.name.isNotEmpty()
            textViewTopic.text = String.format(topicNameTemplate, messagesFilter.topic.name)
        }
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
        with(messagesAdapter) {
            borderPosition = BORDER_POSITION
            onReachStartListener = { store.accept(MessagesScreenEvent.Ui.LoadPreviousPage) }
            onReachEndListener = { store.accept(MessagesScreenEvent.Ui.LoadNextPage) }
            val showChooseActionMenu: (MessageView) -> Unit = {
                store.accept(MessagesScreenEvent.Ui.ShowChooseActionMenu(it))
            }
            val showUserInfo: (MessageView) -> Unit = {
                store.accept(MessagesScreenEvent.Ui.ShowUserInfo(it))
            }
            val updateReaction: (MessageView, ReactionView) -> Unit = { message, reaction ->
                updateReaction(message.messageId, reaction.emoji)
            }
            val loadMessages: (String) -> Unit = {
                loadMessages(it)
            }
            addDelegate(
                UserMessageDelegate(
                    showChooseActionMenu, ::addReaction, updateReaction, showUserInfo
                )
            )
            addDelegate(
                OwnMessageDelegate(
                    showChooseActionMenu, ::addReaction, updateReaction
                )
            )
            addDelegate(DateDelegate())
            addDelegate(MessagesTopicDelegate(topicNameTemplate, loadMessages))
        }
        binding.recyclerViewMessages.adapter = messagesAdapter
        binding.recyclerViewMessages.addItemDecoration(StickyDateInHeaderItemDecoration())
        binding.recyclerViewMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstVisiblePosition = recyclerView.findFirstVisibleItemPosition()
                val lastVisiblePosition = recyclerView.findLastVisibleItemPosition()
                store.accept(
                    MessagesScreenEvent.Ui.MessagesOnScrolled(
                        getVisibleMessagesIds(firstVisiblePosition, lastVisiblePosition),
                        isNextMessageExisting(lastVisiblePosition),
                        isLastMessageVisible(lastVisiblePosition)
                    )
                )
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    store.accept(MessagesScreenEvent.Ui.MessagesScrollStateDragging)
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    store.accept(
                        MessagesScreenEvent.Ui.MessagesScrollStateIdle(
                            recyclerView.canScrollVertically(DIRECTION_UP),
                            recyclerView.canScrollVertically(DIRECTION_DOWN),
                            isNextMessageExisting(recyclerView.findLastVisibleItemPosition())
                        )
                    )
                }
            }
        })
    }

    private fun setupListeners() {
        with(binding) {
            toolbar.setNavigationOnClickListener {
                goBack()
            }
            imageViewTopicArrow.setOnClickListener {
                loadMessages(EMPTY_STRING)
            }
            imageViewAction.setOnClickListener {
                store.accept(
                    MessagesScreenEvent.Ui
                        .SendMessage(messagesFilter, editTextTopicName.text, editTextMessage.text)
                )
            }
            imageViewAction.setOnLongClickListener {
                addAttachment()
                true
            }
            editTextTopicName.doOnTextChanged { text, _, _, _ ->
                store.accept(MessagesScreenEvent.Ui.NewTopicName(text))
            }
            editTextMessage.doOnTextChanged { text, _, _, _ ->
                store.accept(MessagesScreenEvent.Ui.NewMessageText(text))
            }
            fabViewArrow.setOnClickListener {
                recyclerViewMessages.smoothScrollToLastPosition()
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
            val isEditTopicVisible =
                messagesFilter.topic.name.isBlank() && editTextMessage.text.toString().isNotBlank()
            viewNewTopicBorder.isVisible = isEditTopicVisible
            editTextTopicName.isVisible = isEditTopicVisible
            imageViewAction.setImageResource(state.iconActionResId)
            fabViewArrow.isVisible = state.isNextMessageExisting || state.isNewMessageExisting
        }
    }

    override fun handleEffect(effect: MessagesScreenEffect) {
        when (effect) {
            is MessagesScreenEffect.MessageSent -> binding.editTextMessage.text?.clear()
            is MessagesScreenEffect.ScrollToLastMessage ->
                binding.recyclerViewMessages.smoothScrollToLastPosition()

            is MessagesScreenEffect.ShowMessageMenu -> showMessageMenu(effect)
            is MessagesScreenEffect.ShowChooseReactionDialog ->
                showChooseReactionDialog(effect.messageId)

            is MessagesScreenEffect.RawMessageContent ->
                showEditMessageDialog(effect.messageId, effect.content)

            is MessagesScreenEffect.ConfirmDeleteMessage -> confirmDeleteMessage(effect.messageId)
            is MessagesScreenEffect.Failure.ErrorMessages ->
                showError("${getString(R.string.error_messages)} ${effect.value}")

            is MessagesScreenEffect.Failure.ErrorNetwork -> {
                showError("${getString(R.string.error_network)} ${effect.value}")
                showCheckInternetConnectionDialog({ store.accept(MessagesScreenEvent.Ui.Reload) }) {
                    goBack()
                }
            }

            is MessagesScreenEffect.AddAttachment -> addAttachment()
            is MessagesScreenEffect.FileUploaded -> {
                val filename = effect.value.name
                val url = effect.value.url
                val newMessageText = "${binding.editTextMessage.text}\n[$filename]($url)\n"
                binding.editTextMessage.setText(newMessageText)
            }

            is MessagesScreenEffect.FilesDownloaded -> showNotification(effect.value)
        }
    }

    private fun showChooseReactionDialog(messageId: Long) {
        val dialog = ChooseReactionDialogFragment.newInstance(messageId)
        dialog.listener = ::updateReaction
        dialog.show(requireActivity().supportFragmentManager, ChooseReactionDialogFragment.TAG)
    }

    private fun confirmDeleteMessage(messageId: Long) {
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.confirm_delete_message))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                store.accept(MessagesScreenEvent.Ui.DeleteMessage(messageId))
            }
            .setNegativeButton(getString(R.string.no)) { _, _ ->
            }
            .create()
            .show()
    }

    private fun showEditTopicDialog(messageView: MessageView) {
        showInputTextDialog(
            getString(R.string.edit_topic), messageView.messageId, messageView.subject, true
        ) { id, text ->
            store.accept(MessagesScreenEvent.Ui.EditMessageTopic(id, text))
        }
    }

    private fun showEditMessageDialog(messageId: Long, content: String) {
        showInputTextDialog(
            getString(R.string.edit_message), messageId, content, false
        ) { id, text ->
            store.accept(MessagesScreenEvent.Ui.EditMessageContent(id, text))
        }
    }

    private fun showInputTextDialog(
        title: String, messageId: Long, content: String, isTopic: Boolean,
        positiveCallback: (Long, CharSequence) -> Unit,
    ) {
        val input = EditText(requireContext()).apply {
            maxLines = MAX_LINES
            inputType = InputType.TYPE_CLASS_TEXT
            if (isTopic) {
                filters = arrayOf(InputFilter.LengthFilter(TOPIC_MAX_LENGTH))
            } else {
                inputType = inputType or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            }
            setText(content)
        }
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(input)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                positiveCallback(messageId, input.text)
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
            }
            .create()
            .show()
    }

    private fun showNotification(value: Map<String, Boolean>) {
        notificator.createNotificationChannel(CHANNEL_NAME, CHANNEL_ID)
        val title = getString(R.string.downloading_result)
        val error = getString(R.string.error_downloading)
        value.forEach { entry ->
            if (!entry.value) {
                notificator.showNotification(
                    title, CHANNEL_ID, R.drawable.ic_download_error, "${entry.key} - $error"
                )
            }
        }
    }

    private fun showMessageMenu(effect: MessagesScreenEffect.ShowMessageMenu) {
        val popupMenu = PopupMenu(requireContext(), binding.textViewTopic)
        val isMessageWithAttachments = effect.urls.isNotEmpty()
        popupMenu.inflate(R.menu.menu_actions_with_message)
        popupMenu.menu.findItem(R.id.itemSaveAttachments).isVisible = isMessageWithAttachments
        popupMenu.menu.findItem(R.id.itemEditMessage).isVisible = effect.isEditMessageVisible
        popupMenu.menu.findItem(R.id.itemEditTopic).isVisible = effect.isEditTopicVisible
        popupMenu.menu.findItem(R.id.itemDeleteMessage).isVisible = effect.isDeleteMessageVisible
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.itemAddReaction -> {
                    addReaction(effect.messageView)
                    true
                }

                R.id.itemCopyToClipboard -> {
                    store.accept(
                        MessagesScreenEvent.Ui.CopyToClipboard(
                            requireContext(), effect.messageView, isMessageWithAttachments
                        )
                    )
                    true
                }

                R.id.itemEditMessage -> {
                    store.accept(
                        MessagesScreenEvent.Ui.GetRawMessageContent(
                            effect.messageView, isMessageWithAttachments
                        )
                    )
                    true
                }

                R.id.itemEditTopic -> {
                    showEditTopicDialog(effect.messageView)
                    true
                }

                R.id.itemDeleteMessage -> {
                    store.accept(MessagesScreenEvent.Ui.ConfirmDeleteMessage(effect.messageView))
                    true
                }

                R.id.itemSaveAttachments -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        if (externalStoragePermission.isGranted()) {
                            saveAttachments(effect.urls)
                        } else {
                            externalStoragePermission.request { saveAttachments(effect.urls) }
                        }
                    } else {
                        saveAttachments(effect.urls)
                    }
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

    private fun saveAttachments(urls: List<String>) {
        store.accept(MessagesScreenEvent.Ui.SaveAttachments(requireContext(), urls))
    }

    private fun handlePickMediaResult(uri: Uri?) {
        if (uri != null) {
            store.accept(
                MessagesScreenEvent.Ui.UploadFile(requireContext().applicationContext, uri)
            )
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
        val lastVisiblePosition = binding.recyclerViewMessages.findLastVisibleItemPosition()
        store.accept(
            MessagesScreenEvent.Ui.AfterSubmitMessages(
                isNextMessageExisting(lastVisiblePosition),
                isLastMessageVisible(lastVisiblePosition)
            )
        )
    }

    private fun addReaction(messageView: MessageView) {
        store.accept(MessagesScreenEvent.Ui.ShowChooseReactionDialog(messageView))
    }

    private fun updateReaction(messageId: Long, emoji: Emoji) {
        store.accept(MessagesScreenEvent.Ui.UpdateReaction(messageId, emoji))
    }

    private fun goBack() {
        store.accept(MessagesScreenEvent.Ui.Exit)
    }

    private fun parseParams(savedInstanceState: Bundle?) {
        val paramFilter = arguments?.getParam<MessagesFilter>(PARAM_CHANNEL_FILTER)
        if (paramFilter == null || paramFilter.channel.channelId == Channel.UNDEFINED_ID) {
            goBack()
        } else {
            messagesFilter = paramFilter
        }
        savedInstanceState?.let {
            recyclerViewState = it.getParam<Parcelable>(PARAM_RECYCLERVIEW_STATE)
        }
    }

    private fun isMessagesListEmpty(): Boolean {
        return messagesAdapter.itemCount == NO_ITEMS
    }

    private fun RecyclerView.findFirstVisibleItemPosition(): Int {
        return (this.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
    }

    private fun RecyclerView.findLastVisibleItemPosition(): Int {
        return (this.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
    }

    private fun loadMessages(topicName: String) {
        messagesFilter = messagesFilter.copy(topic = Topic(name = topicName))
        setupTopicTitle()
        store.accept(MessagesScreenEvent.Ui.Load(messagesFilter))
    }

    override fun onStart() {
        super.onStart()
        setupOnBackPressedCallback()
    }

    override fun onResume() {
        super.onResume()
        if (isMessagesListEmpty()) {
            loadMessages(messagesFilter.topic.name)
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
        private const val MAX_LINES = 5
        private const val LAST_ITEM_OFFSET = 1
        private const val TOPIC_MAX_LENGTH = 60
        private const val CHANNEL_NAME = "Downloads"
        private const val CHANNEL_ID = "downloads_channel"
        private const val BORDER_POSITION = BuildConfig.MESSAGES_BORDER_POSITION

        fun newInstance(messagesFilter: MessagesFilter): MessagesFragment {
            return MessagesFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(PARAM_CHANNEL_FILTER, messagesFilter)
                }
            }
        }
    }
}