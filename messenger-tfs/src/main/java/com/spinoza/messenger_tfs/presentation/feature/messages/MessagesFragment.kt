package com.spinoza.messenger_tfs.presentation.feature.messages

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.spinoza.messenger_tfs.BuildConfig
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.databinding.DialogAttachFileBinding
import com.spinoza.messenger_tfs.databinding.DialogMessageActionsBinding
import com.spinoza.messenger_tfs.databinding.FragmentMessagesBinding
import com.spinoza.messenger_tfs.databinding.MessagesDialogInputFieldBinding
import com.spinoza.messenger_tfs.di.messages.DaggerMessagesComponent
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.network.WebLimitation
import com.spinoza.messenger_tfs.domain.util.EMPTY_STRING
import com.spinoza.messenger_tfs.domain.util.LAST_ITEM_OFFSET
import com.spinoza.messenger_tfs.domain.util.NO_ITEMS
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
import com.spinoza.messenger_tfs.presentation.feature.messages.notification.Notificator
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.MessageView
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.ReactionView
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.smoothScrollToLastPosition
import com.spinoza.messenger_tfs.presentation.feature.messages.ui.smoothScrollToMessage
import com.spinoza.messenger_tfs.presentation.feature.messages.util.isReadyToSend
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
import com.spinoza.messenger_tfs.presentation.util.showConfirmationDialog
import com.spinoza.messenger_tfs.presentation.util.showError
import com.spinoza.messenger_tfs.presentation.util.showToast
import vivid.money.elmslie.android.base.ElmFragment
import vivid.money.elmslie.android.storeholder.LifecycleAwareStoreHolder
import vivid.money.elmslie.android.storeholder.StoreHolder
import vivid.money.elmslie.coroutines.ElmStoreCompat
import java.util.concurrent.atomic.AtomicBoolean
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

    @Inject
    lateinit var webLimitation: WebLimitation

    private var _binding: FragmentMessagesBinding? = null
    private val binding: FragmentMessagesBinding
        get() = _binding ?: throw RuntimeException("FragmentMessagesBinding == null")

    private var messagesFilter = MessagesFilter()
    private var onBackPressedCallback: OnBackPressedCallback? = null
    private var pickImageLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var pickFileLauncher: ActivityResultLauncher<Intent>? = null
    private var recyclerViewState: Parcelable? = null
    private var recyclerViewStateOnDestroy: Parcelable? = null
    private val isShowingMessageMenu = AtomicBoolean(false)
    private val isShowingAddAttachmentMenu = AtomicBoolean(false)
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
        pickImageLauncher =
            registerForActivityResult(PickVisualMedia()) { handlePickFileResult(it) }
        pickFileLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result != null && result.data != null) {
                    result.data?.let { intent -> handlePickFileResult(intent.data) }
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
        parseParams(savedInstanceState)
        if (savedInstanceState != null) {
            store.accept(MessagesScreenEvent.Ui.CheckLoginStatus)
        }
        setupRecyclerView()
        setupStatusBar()
        setupListeners()
        setupEditTextLimitations()
    }

    private fun setupEditTextLimitations() {
        binding.editTextTopicName.filters =
            arrayOf(InputFilter.LengthFilter(webLimitation.getMaxTopicName()))
        binding.editTextMessage.filters =
            arrayOf(InputFilter.LengthFilter(webLimitation.getMaxMessage()))
    }

    private fun setupTopicTitle() {
        with(binding) {
            val isTopicNameNotEmpty = messagesFilter.topic.name.isNotEmpty()
            textViewTopic.isVisible = isTopicNameNotEmpty
            imageViewTopicArrow.isVisible = isTopicNameNotEmpty
            textViewTopic.text = String.format(topicNameTemplate, messagesFilter.topic.name)
            editTextTopicName.setText(messagesFilter.topic.name)
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
            onReachStartListener = {
                store.accept(MessagesScreenEvent.Ui.LoadPreviousPage)
            }
            onReachEndListener = {
                store.accept(MessagesScreenEvent.Ui.LoadNextPage)
            }
            val showChooseActionMenu: (MessageView) -> Unit = {
                store.accept(MessagesScreenEvent.Ui.ShowChooseActionMenu(it))
            }
            val showUserInfo: (MessageView) -> Unit = {
                store.accept(MessagesScreenEvent.Ui.ShowUserInfo(it))
            }
            val updateReaction: (MessageView, ReactionView) -> Unit = { message, reaction ->
                updateReaction(message.messageId, reaction.emoji)
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
            addDelegate(MessagesTopicDelegate(topicNameTemplate) {
                loadMessages(it)
            })
        }
        binding.recyclerViewMessages.itemAnimator = null
        binding.recyclerViewMessages.adapter = messagesAdapter
        binding.recyclerViewMessages.addItemDecoration(StickyDateInHeaderItemDecoration())
        binding.recyclerViewMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisiblePosition = recyclerView.findLastVisibleItemPosition()
                store.accept(
                    MessagesScreenEvent.Ui.MessagesOnScrolled(
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
                    val firstVisiblePosition = recyclerView.findFirstVisibleItemPosition()
                    val lastVisiblePosition = recyclerView.findLastVisibleItemPosition()
                    store.accept(
                        MessagesScreenEvent.Ui.MessagesScrollStateIdle(
                            getVisibleMessagesIds(firstVisiblePosition, lastVisiblePosition),
                            recyclerView.canScrollVertically(DIRECTION_UP),
                            recyclerView.canScrollVertically(DIRECTION_DOWN),
                            isNextMessageExisting(lastVisiblePosition)
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
                showAddAttachmentMenu()
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
            progressBarMessages.isVisible = state.isLongOperation
            fabViewArrow.isVisible = state.isNextMessageExisting || state.isNewMessageExisting
        }
    }

    override fun handleEffect(effect: MessagesScreenEffect) {
        when (effect) {
            is MessagesScreenEffect.NewMessageDraft -> {
                val isEditTopicVisible =
                    messagesFilter.topic.name.isBlank() && effect.value.content.isNotBlank()
                binding.viewNewTopicBorder.isVisible = isEditTopicVisible
                binding.editTextTopicName.isVisible = isEditTopicVisible
                val iconActionResId = if (effect.value.isReadyToSend(messagesFilter))
                    R.drawable.ic_send
                else
                    R.drawable.ic_add_circle_outline
                binding.imageViewAction.setImageResource(iconActionResId)
            }

            is MessagesScreenEffect.MessageSent -> binding.editTextMessage.text?.clear()
            is MessagesScreenEffect.ScrollToLastMessage ->
                binding.recyclerViewMessages.smoothScrollToLastPosition()

            is MessagesScreenEffect.ShowMessageMenu -> showMessageMenu(effect)
            is MessagesScreenEffect.ShowChooseReactionDialog ->
                showChooseReactionDialog(effect.messageId)

            is MessagesScreenEffect.RawMessageContent ->
                showEditMessageDialog(effect.messageId, effect.content)

            is MessagesScreenEffect.MessageTopicChanged -> if (messagesFilter.topic.name.isNotBlank()) {
                loadMessages(effect.newTopicName)
            }

            is MessagesScreenEffect.ConfirmDeleteMessage -> confirmDeleteMessage(effect.messageId)
            is MessagesScreenEffect.Failure.ErrorMessages ->
                showError(getString(R.string.error_messages), effect.value)

            is MessagesScreenEffect.Failure.ErrorNetwork -> {
                showCheckInternetConnectionDialog(effect.value, {
                    store.accept(MessagesScreenEvent.Ui.Reload)
                    store.accept(MessagesScreenEvent.Ui.OnResume(messagesFilter))
                }) {
                    goBack()
                }
            }

            is MessagesScreenEffect.AddAttachment -> showAddAttachmentMenu()
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
        showConfirmationDialog(
            message = getString(R.string.confirm_delete_message),
            onPositiveClickCallback = {
                showToast(getString(R.string.message_delete_request_sent))
                store.accept(MessagesScreenEvent.Ui.DeleteMessage(messageId))
            }
        )
    }

    private fun showEditTopicDialog(messageView: MessageView) {
        showInputTextDialog(
            getString(R.string.edit_topic),
            messageView.messageId,
            messageView.subject,
            false,
            webLimitation.getMaxTopicName()
        ) { id, text ->
            store.accept(MessagesScreenEvent.Ui.EditMessageTopic(id, messageView.subject, text))
        }
    }

    private fun showEditMessageDialog(messageId: Long, content: String) {
        showInputTextDialog(
            getString(R.string.edit_message),
            messageId,
            content,
            true,
            webLimitation.getMaxMessage()
        ) { id, text ->
            store.accept(MessagesScreenEvent.Ui.EditMessageContent(id, content, text))
        }
    }

    private fun showInputTextDialog(
        title: String, messageId: Long, content: String, isMessage: Boolean, maxLength: Int,
        positiveCallback: (Long, CharSequence?) -> Unit,
    ) {
        val inputField = MessagesDialogInputFieldBinding.inflate(layoutInflater)
        with(inputField.input) {
            maxLines = MAX_LINES
            inputType = InputType.TYPE_CLASS_TEXT
            filters = arrayOf(InputFilter.LengthFilter(maxLength))
            if (isMessage) {
                inputType = inputType or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            }
            setText(content)
        }
        showConfirmationDialog(
            title = title,
            positiveButtonTitleResId = R.string.save,
            negativeButtonTitleResId = R.string.cancel,
            view = inputField.root,
            onPositiveClickCallback = { positiveCallback(messageId, inputField.input.text) }
        )
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
        if (isShowingMessageMenu.get()) return
        isShowingMessageMenu.set(true)
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = DialogMessageActionsBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        val isMessageWithAttachments = effect.urls.isNotEmpty()
        with(dialogBinding) {
            itemSaveAttachments.isVisible = isMessageWithAttachments
            itemEditMessage.isVisible = effect.isEditMessageVisible
            itemEditTopic.isVisible = effect.isEditTopicVisible
            itemDeleteMessage.isVisible = effect.isDeleteMessageVisible
            itemAddReaction.setOnClickListener {
                addReaction(effect.messageView)
                dialog.dismiss()
            }
            itemCopyToClipboard.setOnClickListener {
                store.accept(
                    MessagesScreenEvent.Ui.CopyToClipboard(
                        requireContext(), effect.messageView, isMessageWithAttachments
                    )
                )
                dialog.dismiss()
            }
            itemEditMessage.setOnClickListener {
                store.accept(
                    MessagesScreenEvent.Ui.GetRawMessageContent(
                        effect.messageView, isMessageWithAttachments
                    )
                )
                dialog.dismiss()
            }
            itemEditTopic.setOnClickListener {
                showEditTopicDialog(effect.messageView)
                dialog.dismiss()
            }
            itemDeleteMessage.setOnClickListener {
                store.accept(MessagesScreenEvent.Ui.ConfirmDeleteMessage(effect.messageView))
                dialog.dismiss()
            }
            itemSaveAttachments.setOnClickListener {
                if (externalStoragePermission.isGranted(ExternalStoragePermission.Type.WRITE)) {
                    saveAttachments(effect.urls)
                } else {
                    externalStoragePermission.request(ExternalStoragePermission.Type.WRITE) {
                        saveAttachments(effect.urls)
                    }
                }
                dialog.dismiss()
            }
        }
        dialog.setOnDismissListener {
            isShowingMessageMenu.set(false)
        }
        dialog.show()
    }

    private fun showAddAttachmentMenu() {
        if (isShowingAddAttachmentMenu.get()) return
        isShowingAddAttachmentMenu.set(true)
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = DialogAttachFileBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        with(dialogBinding) {
            itemAttachImage.setOnClickListener {
                pickImageLauncher?.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                dialog.dismiss()
            }
            itemAttachFile.setOnClickListener {
                if (externalStoragePermission.isGranted(ExternalStoragePermission.Type.READ)) {
                    pickFile()
                } else {
                    externalStoragePermission.request(ExternalStoragePermission.Type.READ) {
                        pickFile()
                    }
                }
                dialog.dismiss()
            }
        }
        dialog.setOnDismissListener {
            isShowingAddAttachmentMenu.set(false)
        }
        dialog.show()
    }

    private fun pickFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = TYPE_ALL_FILES
        }
        pickFileLauncher?.launch(intent)
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

    private fun saveAttachments(urls: List<String>) {
        showToast(getString(R.string.download_will_start_soon))
        store.accept(MessagesScreenEvent.Ui.SaveAttachments(requireContext(), urls))
    }

    private fun handlePickFileResult(uri: Uri?) {
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
        store.accept(MessagesScreenEvent.Ui.OnResume(messagesFilter))
    }

    override fun onPause() {
        super.onPause()
        binding.shimmerLarge.off()
        binding.shimmerSending.off()
        store.accept(MessagesScreenEvent.Ui.OnPause)
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
        private const val MAX_LINES = 5
        private const val CHANNEL_NAME = "Downloads"
        private const val CHANNEL_ID = "downloads_channel"
        private const val TYPE_ALL_FILES = "*/*"
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