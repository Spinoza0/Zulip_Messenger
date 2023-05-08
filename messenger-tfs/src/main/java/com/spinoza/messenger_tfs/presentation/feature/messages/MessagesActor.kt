package com.spinoza.messenger_tfs.presentation.feature.messages

import android.content.ClipData
import android.content.Context
import android.text.Html
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.di.DispatcherDefault
import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesPageType
import com.spinoza.messenger_tfs.domain.model.MessagesResult
import com.spinoza.messenger_tfs.domain.model.RepositoryError
import com.spinoza.messenger_tfs.domain.model.event.DeleteMessageEvent
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.MessageEvent
import com.spinoza.messenger_tfs.domain.model.event.ReactionEvent
import com.spinoza.messenger_tfs.domain.model.event.UpdateMessageEvent
import com.spinoza.messenger_tfs.domain.network.AuthorizationStorage
import com.spinoza.messenger_tfs.domain.usecase.event.DeleteEventQueueUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.EventUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.GetDeleteMessageEventUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.GetMessageEventUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.GetReactionEventUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.GetUpdateMessageEventUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.RegisterEventQueueUseCase
import com.spinoza.messenger_tfs.domain.usecase.login.LogInUseCase
import com.spinoza.messenger_tfs.domain.usecase.messages.DeleteMessageUseCase
import com.spinoza.messenger_tfs.domain.usecase.messages.EditMessageUseCase
import com.spinoza.messenger_tfs.domain.usecase.messages.GetMessageRawContentUseCase
import com.spinoza.messenger_tfs.domain.usecase.messages.GetMessagesUseCase
import com.spinoza.messenger_tfs.domain.usecase.messages.GetStoredMessagesUseCase
import com.spinoza.messenger_tfs.domain.usecase.messages.GetUpdatedMessageFilterUserCase
import com.spinoza.messenger_tfs.domain.usecase.messages.SaveAttachmentsUseCase
import com.spinoza.messenger_tfs.domain.usecase.messages.SendMessageUseCase
import com.spinoza.messenger_tfs.domain.usecase.messages.SetMessagesFlagToReadUserCase
import com.spinoza.messenger_tfs.domain.usecase.messages.SetOwnStatusActiveUseCase
import com.spinoza.messenger_tfs.domain.usecase.messages.UpdateReactionUseCase
import com.spinoza.messenger_tfs.domain.usecase.messages.UploadFileUseCase
import com.spinoza.messenger_tfs.domain.util.EMPTY_STRING
import com.spinoza.messenger_tfs.domain.util.getText
import com.spinoza.messenger_tfs.presentation.adapter.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.date.DateDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages.OwnMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages.UserMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.topic.MessagesTopicDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesResultDelegate
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenEvent
import com.spinoza.messenger_tfs.presentation.util.EventsQueueHolder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vivid.money.elmslie.coroutines.Actor
import java.util.TreeSet
import javax.inject.Inject

class MessagesActor @Inject constructor(
    lifecycle: Lifecycle,
    private val authorizationStorage: AuthorizationStorage,
    private val logInUseCase: LogInUseCase,
    private val getStoredMessagesUseCase: GetStoredMessagesUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val getMessageRawContentUseCase: GetMessageRawContentUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val editMessageUseCase: EditMessageUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val updateReactionUseCase: UpdateReactionUseCase,
    private val getMessageEventUseCase: GetMessageEventUseCase,
    private val getUpdateMessageEventUseCase: GetUpdateMessageEventUseCase,
    private val getDeleteMessageEventUseCase: GetDeleteMessageEventUseCase,
    private val getReactionEventUseCase: GetReactionEventUseCase,
    private val setOwnStatusActiveUseCase: SetOwnStatusActiveUseCase,
    private val setMessagesFlagToReadUserCase: SetMessagesFlagToReadUserCase,
    private val getUpdatedMessageFilterUserCase: GetUpdatedMessageFilterUserCase,
    private val uploadFileUseCase: UploadFileUseCase,
    private val saveAttachmentsUseCase: SaveAttachmentsUseCase,
    registerEventQueueUseCase: RegisterEventQueueUseCase,
    deleteEventQueueUseCase: DeleteEventQueueUseCase,
    @DispatcherDefault private val defaultDispatcher: CoroutineDispatcher,
) : Actor<MessagesScreenCommand, MessagesScreenEvent.Internal> {

    private var messagesFilter: MessagesFilter = MessagesFilter()
    private val lifecycleScope = lifecycle.coroutineScope
    private val newMessageFieldState = MutableSharedFlow<String>()
    private var messagesQueue: EventsQueueHolder =
        EventsQueueHolder(lifecycleScope, registerEventQueueUseCase, deleteEventQueueUseCase)
    private var updateMessagesQueue: EventsQueueHolder =
        EventsQueueHolder(lifecycleScope, registerEventQueueUseCase, deleteEventQueueUseCase)
    private var deleteMessagesQueue: EventsQueueHolder =
        EventsQueueHolder(lifecycleScope, registerEventQueueUseCase, deleteEventQueueUseCase)
    private var reactionsQueue: EventsQueueHolder =
        EventsQueueHolder(lifecycleScope, registerEventQueueUseCase, deleteEventQueueUseCase)
    private var iconActionResId = R.drawable.ic_add_circle_outline
    private var isIconActionResIdChanged = false
    private var lastLoadCommand: MessagesScreenCommand? = null

    @Volatile
    private var isLoadingFirstPage = false

    @Volatile
    private var isLoadingPreviousPage = false

    @Volatile
    private var isLoadingNextPage = false

    @Volatile
    private var isLoadingLastPage = false

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            lifecycleScope.launch {
                messagesQueue.deleteQueue()
                updateMessagesQueue.deleteQueue()
                deleteMessagesQueue.deleteQueue()
                reactionsQueue.deleteQueue()
            }
        }
    }

    init {
        lifecycle.addObserver(lifecycleObserver)
        subscribeToNewMessageFieldChanges()
        startUpdatingInfo()
    }

    override fun execute(command: MessagesScreenCommand): Flow<MessagesScreenEvent.Internal> =
        flow {
            val event = when (command) {
                is MessagesScreenCommand.NewMessageText -> newMessageText(command.value)
                is MessagesScreenCommand.LoadFirstPage -> loadFirstPage(command)
                is MessagesScreenCommand.LoadPreviousPage -> loadPreviousPage(command)
                is MessagesScreenCommand.LoadNextPage -> loadNextPage(command)
                is MessagesScreenCommand.LoadLastPage -> loadLastPage(command)
                is MessagesScreenCommand.SetMessagesRead -> setMessageReadFlags(command.messageIds)
                is MessagesScreenCommand.UpdateReaction ->
                    updateReaction(command.messageId, command.emoji)

                is MessagesScreenCommand.SendMessage -> sendMessage(command.value)
                is MessagesScreenCommand.GetMessagesEvent ->
                    getMessagesEvent(command.isLastMessageVisible)

                is MessagesScreenCommand.GetUpdateMessagesEvent ->
                    getUpdateMessagesEvent(command.isLastMessageVisible)

                is MessagesScreenCommand.GetDeleteMessagesEvent ->
                    getDeleteMessagesEvent(command.isLastMessageVisible)

                is MessagesScreenCommand.GetReactionsEvent ->
                    getReactionsEvent(command.isLastMessageVisible)

                is MessagesScreenCommand.IsNextPageExisting -> isNextPageExisting(command)
                is MessagesScreenCommand.Reload -> {
                    delay(DELAY_BEFORE_RELOAD)
                    var result: MessagesScreenEvent.Internal = MessagesScreenEvent.Internal.Idle
                    lastLoadCommand?.let { lastCommand ->
                        when (lastCommand) {
                            is MessagesScreenCommand.LoadFirstPage ->
                                result = loadFirstPage(lastCommand)

                            is MessagesScreenCommand.LoadPreviousPage -> {
                                lastLoadCommand = null
                                result = loadPreviousPage(lastCommand)
                            }

                            is MessagesScreenCommand.LoadNextPage -> {
                                lastLoadCommand = null
                                result = loadNextPage(lastCommand)
                            }

                            is MessagesScreenCommand.LoadLastPage -> {
                                lastLoadCommand = null
                                result = loadLastPage(lastCommand)
                            }

                            else -> throw RuntimeException("Invalid command: $lastCommand")
                        }
                    }
                    if (result is MessagesScreenEvent.Internal.Idle) {
                        delay(DELAY_BEFORE_RETURN_IDLE_EVENT)
                    }
                    result
                }

                is MessagesScreenCommand.LoadStored -> {
                    messagesFilter = command.filter
                    loadStoredMessages()
                }

                is MessagesScreenCommand.UploadFile -> uploadFile(command)
                is MessagesScreenCommand.CopyToClipboard -> copyToClipboard(command)
                is MessagesScreenCommand.EditMessageContent -> editMessageContent(command)
                is MessagesScreenCommand.EditMessageTopic -> editMessageTopic(command)
                is MessagesScreenCommand.GetRawMessageContent -> getRawMessageContent(command)
                is MessagesScreenCommand.SaveAttachments -> saveAttachments(command)
                is MessagesScreenCommand.DeleteMessage -> deleteMessage(command.messageId)
                is MessagesScreenCommand.LogIn -> logIn()
            }
            emit(event)
        }

    private suspend fun logIn(): MessagesScreenEvent.Internal {
        var event: MessagesScreenEvent.Internal = MessagesScreenEvent.Internal.Idle
        logInUseCase(
            authorizationStorage.getEmail(),
            authorizationStorage.getPassword()
        ).onSuccess {
            event = MessagesScreenEvent.Internal.LoginSuccess
        }.onFailure { error ->
            event = if (error is RepositoryError) {
                MessagesScreenEvent.Internal.LogOut
            } else {
                MessagesScreenEvent.Internal.ErrorNetwork(error.getText())
            }
        }
        return event
    }

    private suspend fun getIdleEvent(): MessagesScreenEvent.Internal.Idle {
        delay(DELAY_BEFORE_RETURN_IDLE_EVENT)
        return MessagesScreenEvent.Internal.Idle
    }

    private suspend fun isNextPageExisting(
        command: MessagesScreenCommand.IsNextPageExisting,
    ): MessagesScreenEvent.Internal {
        if (command.messagesResultDelegate.messages.isEmpty()) return getIdleEvent()
        val lastItem = command.messagesResultDelegate.messages.last()
        if (lastItem !is UserMessageDelegateItem && lastItem !is OwnMessageDelegateItem) {
            return getIdleEvent()
        }
        val lastMessage = lastItem.content() as Message
        val result = if (command.messageSentId != Message.UNDEFINED_ID) {
            lastMessage.id != command.messageSentId
        } else {
            lastMessage.id != messagesFilter.topic.lastMessageId
        }
        return MessagesScreenEvent.Internal.NextPageExists(result, command.isGoingToLastMessage)
    }

    private suspend fun loadStoredMessages(): MessagesScreenEvent.Internal =
        withContext(defaultDispatcher) {
            getStoredMessagesUseCase(messagesFilter).onSuccess { messagesResult ->
                return@withContext handleMessages(messagesResult, MessagesPageType.STORED)
            }.onFailure { error ->
                return@withContext handleErrors(error)
            }
            getIdleEvent()
        }

    private suspend fun loadFirstPage(
        command: MessagesScreenCommand,
    ): MessagesScreenEvent.Internal {
        if (isLoadingFirstPage) return getIdleEvent()
        isLoadingFirstPage = true
        lastLoadCommand = command
        val messagesPageType =
            if ((command as MessagesScreenCommand.LoadFirstPage).isMessagesListEmpty) {
                MessagesPageType.FIRST_UNREAD
            } else {
                MessagesPageType.AFTER_STORED
            }
        val event = loadMessages(messagesPageType)
        isLoadingFirstPage = false
        if (event is MessagesScreenEvent.Internal.Messages) {
            registerEventQueues()
        }
        return event
    }

    private suspend fun loadPreviousPage(
        command: MessagesScreenCommand,
    ): MessagesScreenEvent.Internal {
        if (isLoadingPreviousPage) return getIdleEvent()
        isLoadingPreviousPage = true
        lastLoadCommand = command
        val event = loadMessages(MessagesPageType.OLDEST)
        isLoadingPreviousPage = false
        return event
    }

    private suspend fun loadNextPage(
        command: MessagesScreenCommand,
    ): MessagesScreenEvent.Internal {
        if (isLoadingNextPage) return getIdleEvent()
        isLoadingNextPage = true
        lastLoadCommand = command
        val event = loadMessages(MessagesPageType.NEWEST)
        isLoadingNextPage = false
        return event
    }

    private suspend fun loadLastPage(
        command: MessagesScreenCommand,
    ): MessagesScreenEvent.Internal {
        if (isLoadingLastPage) return getIdleEvent()
        isLoadingLastPage = true
        lastLoadCommand = command
        val event = loadMessages(MessagesPageType.LAST)
        isLoadingLastPage = false
        return event
    }

    private suspend fun newMessageText(text: CharSequence?): MessagesScreenEvent.Internal {
        newMessageFieldState.emit(text.toString())
        delay(DELAY_BEFORE_CHECK_ACTION_ICON)
        if (isIconActionResIdChanged) {
            isIconActionResIdChanged = false
            return MessagesScreenEvent.Internal.IconActionResId(iconActionResId)
        }
        return getIdleEvent()
    }

    private suspend fun sendMessage(value: String): MessagesScreenEvent.Internal {
        if (value.isNotEmpty()) {
            sendMessageUseCase(value, messagesFilter).onSuccess { messageId ->
                return MessagesScreenEvent.Internal.MessageSent(messageId)
            }.onFailure { error ->
                return handleErrors(error)
            }
        }
        return getIdleEvent()
    }

    private suspend fun deleteMessage(messageId: Long): MessagesScreenEvent.Internal {
        deleteMessageUseCase(messageId).onFailure { error ->
            return handleErrors(error)
        }
        return getIdleEvent()
    }

    private suspend fun updateReaction(
        messageId: Long,
        emoji: Emoji,
    ): MessagesScreenEvent.Internal =
        withContext(defaultDispatcher) {
            updateReactionUseCase(messageId, emoji, messagesFilter).onSuccess { messagesResult ->
                return@withContext MessagesScreenEvent.Internal.Messages(messagesResult.toDelegate())
            }
            getIdleEvent()
        }

    private suspend fun loadMessages(messagesPageType: MessagesPageType): MessagesScreenEvent.Internal =
        withContext(defaultDispatcher) {
            getMessagesUseCase(messagesPageType, messagesFilter).onSuccess { messagesResult ->
                return@withContext handleMessages(messagesResult, messagesPageType)
            }.onFailure { error ->
                return@withContext handleErrors(error)
            }
            getIdleEvent()
        }

    private suspend fun setMessageReadFlags(messageIds: List<Long>): MessagesScreenEvent.Internal {
        setMessagesFlagToReadUserCase(messageIds)
        return getIdleEvent()
    }

    private fun startUpdatingInfo() {
        lifecycleScope.launch {
            while (isActive) {
                setOwnStatusActiveUseCase()
                delay(DELAY_BEFORE_UPDATE_MESSAGE_FILTER)
                messagesFilter = getUpdatedMessageFilterUserCase(messagesFilter)
                delay(DELAY_AFTER_UPDATE_MESSAGE_FILTER)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun subscribeToNewMessageFieldChanges() {
        newMessageFieldState
            .distinctUntilChanged()
            .debounce(DELAY_BEFORE_UPDATE_ACTION_ICON)
            .mapLatest { text ->
                val resId = if (text.isNotBlank())
                    R.drawable.ic_send
                else
                    R.drawable.ic_add_circle_outline
                resId
            }
            .distinctUntilChanged()
            .onEach { resId ->
                isIconActionResIdChanged = true
                iconActionResId = resId
            }
            .flowOn(defaultDispatcher)
            .launchIn(lifecycleScope)
    }

    private suspend fun getMessagesEvent(
        isLastMessageVisible: Boolean,
    ): MessagesScreenEvent.Internal {
        return getEvent(
            messagesQueue, getMessageEventUseCase, ::onSuccessMessageEvent,
            MessagesScreenEvent.Internal.EmptyMessagesQueueEvent,
            isLastMessageVisible
        )
    }

    private suspend fun getUpdateMessagesEvent(
        isLastMessageVisible: Boolean,
    ): MessagesScreenEvent.Internal {
        return getEvent(
            updateMessagesQueue, getUpdateMessageEventUseCase, ::onSuccessUpdateMessageEvent,
            MessagesScreenEvent.Internal.EmptyUpdateMessagesQueueEvent,
            isLastMessageVisible
        )
    }

    private suspend fun getDeleteMessagesEvent(
        isLastMessageVisible: Boolean,
    ): MessagesScreenEvent.Internal {
        return getEvent(
            deleteMessagesQueue, getDeleteMessageEventUseCase, ::onSuccessDeleteMessageEvent,
            MessagesScreenEvent.Internal.EmptyDeleteMessagesQueueEvent,
            isLastMessageVisible
        )
    }

    private suspend fun getReactionsEvent(
        isLastMessageVisible: Boolean,
    ): MessagesScreenEvent.Internal {
        return getEvent(
            reactionsQueue, getReactionEventUseCase, ::onSuccessReactionEvent,
            MessagesScreenEvent.Internal.EmptyReactionsQueueEvent,
            isLastMessageVisible
        )
    }

    private fun onSuccessMessageEvent(
        eventsQueue: EventsQueueHolder,
        event: MessageEvent,
    ): MessagesScreenEvent.Internal {
        updateLastEventId(eventsQueue, event.lastEventId)
        return MessagesScreenEvent.Internal.MessagesEventFromQueue(
            event.messagesResult.toDelegate()
        )
    }

    private fun onSuccessUpdateMessageEvent(
        eventsQueue: EventsQueueHolder,
        event: UpdateMessageEvent,
    ): MessagesScreenEvent.Internal {
        updateLastEventId(eventsQueue, event.lastEventId)
        return MessagesScreenEvent.Internal.UpdateMessagesEventFromQueue(
            event.messagesResult.toDelegate()
        )
    }

    private fun onSuccessDeleteMessageEvent(
        eventsQueue: EventsQueueHolder,
        event: DeleteMessageEvent,
    ): MessagesScreenEvent.Internal {
        updateLastEventId(eventsQueue, event.lastEventId)
        return MessagesScreenEvent.Internal.DeleteMessagesEventFromQueue(
            event.messagesResult.toDelegate()
        )
    }

    private fun onSuccessReactionEvent(
        eventsQueue: EventsQueueHolder,
        event: ReactionEvent,
    ): MessagesScreenEvent.Internal {
        updateLastEventId(eventsQueue, event.lastEventId)
        return MessagesScreenEvent.Internal.ReactionsEventFromQueue(
            event.messagesResult.toDelegate()
        )
    }

    private fun updateLastEventId(eventsQueue: EventsQueueHolder, lastEventId: Long) {
        eventsQueue.queue = eventsQueue.queue.copy(lastEventId = lastEventId)
    }

    private suspend fun <T> getEvent(
        eventsQueue: EventsQueueHolder,
        useCase: EventUseCase<T>,
        onSuccessCallback: (EventsQueueHolder, T) -> MessagesScreenEvent.Internal,
        emptyEvent: MessagesScreenEvent.Internal,
        isLastMessageVisible: Boolean,
    ): MessagesScreenEvent.Internal = withContext(defaultDispatcher) {
        if (eventsQueue.queue.queueId.isNotEmpty()) useCase(
            eventsQueue.queue,
            messagesFilter,
            isLastMessageVisible
        ).onSuccess { event ->
            return@withContext onSuccessCallback(eventsQueue, event)
        }.onFailure {
            registerEventQueues()
        }
        delay(DELAY_BEFORE_CHECK_EVENTS)
        emptyEvent
    }

    private fun MessagesResult.toDelegate(): MessagesResultDelegate {
        return MessagesResultDelegate(
            messages.groupByDate(authorizationStorage.getUserId()),
            position,
            isNewMessageExisting
        )
    }

    private fun handleMessages(
        messagesResult: MessagesResult,
        messagesPageType: MessagesPageType,
    ): MessagesScreenEvent.Internal {
        val messagesResultDelegate = messagesResult.toDelegate()
        if (messagesPageType == MessagesPageType.STORED) {
            return MessagesScreenEvent.Internal.StoredMessages(messagesResultDelegate)
        }
        return MessagesScreenEvent.Internal.Messages(messagesResultDelegate)
    }

    private fun registerEventQueues() {
        messagesQueue.registerQueue(listOf(EventType.MESSAGE))
        updateMessagesQueue.registerQueue(listOf(EventType.UPDATE_MESSAGE))
        deleteMessagesQueue.registerQueue(listOf(EventType.DELETE_MESSAGE))
        reactionsQueue.registerQueue(listOf(EventType.REACTION))
    }

    private suspend fun uploadFile(
        command: MessagesScreenCommand.UploadFile,
    ): MessagesScreenEvent.Internal {
        uploadFileUseCase(command.context, command.uri).onSuccess {
            return MessagesScreenEvent.Internal.FileUploaded(it)
        }.onFailure {
            return handleErrors(it)
        }
        return getIdleEvent()
    }

    private suspend fun copyToClipboard(
        command: MessagesScreenCommand.CopyToClipboard,
    ): MessagesScreenEvent.Internal {
        val context = command.context
        val text =
            getRawMessageText(command.messageId, command.content, command.isMessageWithAttachments)
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = ClipData.newPlainText(context.getString(R.string.message), text)
        clipboard.setPrimaryClip(clip)
        return getIdleEvent()
    }

    private suspend fun editMessageContent(
        command: MessagesScreenCommand.EditMessageContent,
    ): MessagesScreenEvent.Internal {
        editMessageUseCase(command.messageId, content = command.content.toString()).onFailure {
            return handleErrors(it)
        }
        return getIdleEvent()
    }

    private suspend fun editMessageTopic(
        command: MessagesScreenCommand.EditMessageTopic,
    ): MessagesScreenEvent.Internal {
        editMessageUseCase(command.messageId, topic = command.topic.toString()).onFailure {
            return handleErrors(it)
        }
        return getIdleEvent()
    }

    private suspend fun getRawMessageContent(
        command: MessagesScreenCommand.GetRawMessageContent,
    ): MessagesScreenEvent.Internal {
        val text =
            getRawMessageText(command.messageId, command.content, command.isMessageWithAttachments)
        return MessagesScreenEvent.Internal.RawMessageContent(command.messageId, text)
    }

    private suspend fun getRawMessageText(
        messageId: Long, default: String, isMessageWithAttachments: Boolean,
    ): String {
        var text = Html.fromHtml(default, Html.FROM_HTML_MODE_COMPACT).toString()
        if (text.endsWith(LINE_FEED)) {
            text = text.dropLast(LINE_FEED.length)
        }
        return if (isMessageWithAttachments) {
            getMessageRawContentUseCase(messageId, text)
        } else {
            text
        }
    }

    private suspend fun saveAttachments(
        command: MessagesScreenCommand.SaveAttachments,
    ): MessagesScreenEvent.Internal {
        return MessagesScreenEvent.Internal.FilesDownloaded(
            saveAttachmentsUseCase(command.context, command.urls)
        )
    }

    private fun handleErrors(error: Throwable): MessagesScreenEvent.Internal {
        return if (error is RepositoryError) {
            MessagesScreenEvent.Internal.ErrorMessages(error.value)
        } else {
            MessagesScreenEvent.Internal.ErrorNetwork(error.getText())
        }
    }

    private fun List<Message>.groupByDate(userId: Long): List<DelegateAdapterItem> {
        val messageAdapterItemList = mutableListOf<DelegateAdapterItem>()
        val dates = TreeSet<MessageDate>()
        forEach {
            dates.add(it.date)
        }
        var lastTopicName = EMPTY_STRING
        dates.forEach { messageDate ->
            messageAdapterItemList.add(DateDelegateItem(messageDate))
            var isDateChanged = true
            val allDayMessages = this.filter { message ->
                message.date.dateString == messageDate.dateString
            }
            allDayMessages.forEach { message ->
                if (messagesFilter.topic.name.isEmpty() &&
                    (isDateChanged || !lastTopicName.equals(message.subject, ignoreCase = true))
                ) {
                    lastTopicName = message.subject
                    messageAdapterItemList.add(MessagesTopicDelegateItem(lastTopicName))
                }
                if (message.user.userId == userId) {
                    messageAdapterItemList.add(OwnMessageDelegateItem(message))
                } else {
                    messageAdapterItemList.add(UserMessageDelegateItem(message))
                }
                isDateChanged = false
            }
        }
        return messageAdapterItemList
    }

    private companion object {

        const val DELAY_BEFORE_UPDATE_ACTION_ICON = 200L
        const val DELAY_BEFORE_CHECK_ACTION_ICON = 300L
        const val DELAY_BEFORE_RETURN_IDLE_EVENT = 1000L
        const val DELAY_BEFORE_CHECK_EVENTS = 1000L
        const val DELAY_BEFORE_RELOAD = 500L
        const val DELAY_BEFORE_UPDATE_MESSAGE_FILTER = 55_000L
        const val DELAY_AFTER_UPDATE_MESSAGE_FILTER = 5_000L
        const val LINE_FEED = "\n"
    }
}