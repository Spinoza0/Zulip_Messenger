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
import com.spinoza.messenger_tfs.domain.model.MessageDateTime
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesPageType
import com.spinoza.messenger_tfs.domain.model.MessagesResult
import com.spinoza.messenger_tfs.domain.model.RepositoryError
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.model.event.DeleteMessageEvent
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.MessageEvent
import com.spinoza.messenger_tfs.domain.model.event.ReactionEvent
import com.spinoza.messenger_tfs.domain.model.event.UpdateMessageEvent
import com.spinoza.messenger_tfs.domain.network.AuthorizationStorage
import com.spinoza.messenger_tfs.domain.network.WebLimitation
import com.spinoza.messenger_tfs.domain.usecase.channels.GetTopicsUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.DeleteEventQueueUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.GetDeleteMessageEventUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.GetMessageEventUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.GetReactionEventUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.GetUpdateMessageEventUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.MessagesEventUseCase
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
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessageDraft
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesResultDelegate
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenEvent
import com.spinoza.messenger_tfs.presentation.util.EventsQueueHolder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vivid.money.elmslie.coroutines.Actor
import java.util.TreeSet
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class MessagesActor @Inject constructor(
    lifecycle: Lifecycle,
    private val authorizationStorage: AuthorizationStorage,
    private val webLimitation: WebLimitation,
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
    private val getTopicsUseCase: GetTopicsUseCase,
    registerEventQueueUseCase: RegisterEventQueueUseCase,
    deleteEventQueueUseCase: DeleteEventQueueUseCase,
    @DispatcherDefault private val defaultDispatcher: CoroutineDispatcher,
) : Actor<MessagesScreenCommand, MessagesScreenEvent.Internal> {

    private var messagesFilter: MessagesFilter = MessagesFilter()
    private val lifecycleScope = lifecycle.coroutineScope
    private val newMessageDraftState = MutableSharedFlow<MessageDraft>()
    private var messagesQueue: EventsQueueHolder =
        EventsQueueHolder(lifecycleScope, registerEventQueueUseCase, deleteEventQueueUseCase)
    private var updateMessagesQueue: EventsQueueHolder =
        EventsQueueHolder(lifecycleScope, registerEventQueueUseCase, deleteEventQueueUseCase)
    private var deleteMessagesQueue: EventsQueueHolder =
        EventsQueueHolder(lifecycleScope, registerEventQueueUseCase, deleteEventQueueUseCase)
    private var reactionsQueue: EventsQueueHolder =
        EventsQueueHolder(lifecycleScope, registerEventQueueUseCase, deleteEventQueueUseCase)
    private var messageDraft = MessageDraft(EMPTY_STRING, EMPTY_STRING)
    private var isMessageDraftChanged = false
    private var lastLoadCommand: MessagesScreenCommand? = null
    private var updatingInfoJob: Job? = null
    private var isLoadingMessages = AtomicBoolean(false)

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            lifecycleScope.launch {
                unsubscribeFromEvents()
                updatingInfoJob?.cancel()
            }
        }
    }

    init {
        lifecycle.addObserver(lifecycleObserver)
        subscribeToNewMessageDraftChanges()
        startUpdatingInfo()
    }

    override fun execute(command: MessagesScreenCommand): Flow<MessagesScreenEvent.Internal> =
        flow {
            val event = when (command) {
                is MessagesScreenCommand.NewMessageText -> newMessageText(command.value)
                is MessagesScreenCommand.NewTopicName -> newTopicName(command.value)
                is MessagesScreenCommand.LoadFirstPage -> loadFirstPage(command)
                is MessagesScreenCommand.LoadPreviousPage -> loadPreviousPage(command)
                is MessagesScreenCommand.LoadCurrentWithPreviousPage ->
                    loadCurrentWithPreviousPage(command)

                is MessagesScreenCommand.LoadNextPage -> loadNextPage(command)
                is MessagesScreenCommand.LoadCurrentWithNextPage -> loadCurrentWithNextPage(command)
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

                            is MessagesScreenCommand.LoadCurrentWithPreviousPage -> {
                                lastLoadCommand = null
                                result = loadCurrentWithPreviousPage(lastCommand)
                            }

                            is MessagesScreenCommand.LoadNextPage -> {
                                lastLoadCommand = null
                                result = loadNextPage(lastCommand)
                            }

                            is MessagesScreenCommand.LoadCurrentWithNextPage -> {
                                lastLoadCommand = null
                                result = loadCurrentWithNextPage(lastCommand)
                            }

                            is MessagesScreenCommand.LoadLastPage -> {
                                lastLoadCommand = null
                                result = loadLastPage(lastCommand)
                            }

                            else -> throw RuntimeException("Invalid command: $lastCommand")
                        }
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
                is MessagesScreenCommand.GetTopics -> getTopics(command)
                is MessagesScreenCommand.SubscribeOnEvents -> subscribeOnEvents(command.filter)
                is MessagesScreenCommand.UnsubscribeFromEvents -> unsubscribeFromEvents()
                is MessagesScreenCommand.LogIn -> logIn()
            }
            emit(event)
        }

    private fun subscribeOnEvents(filter: MessagesFilter): MessagesScreenEvent.Internal {
        val filterWithoutTopic = filter.copy(topic = Topic())
        messagesQueue.registerQueue(
            eventTypes = listOf(EventType.MESSAGE),
            filter = filterWithoutTopic
        )
        updateMessagesQueue.registerQueue(
            eventTypes = listOf(EventType.UPDATE_MESSAGE),
            filter = filterWithoutTopic
        )
        deleteMessagesQueue.registerQueue(
            eventTypes = listOf(EventType.DELETE_MESSAGE),
            filter = filterWithoutTopic
        )
        reactionsQueue.registerQueue(
            eventTypes = listOf(EventType.REACTION),
            filter = filterWithoutTopic
        )
        return MessagesScreenEvent.Internal.SubscribedOnEvents
    }

    private suspend fun unsubscribeFromEvents(): MessagesScreenEvent.Internal {
        messagesQueue.deleteQueue()
        updateMessagesQueue.deleteQueue()
        deleteMessagesQueue.deleteQueue()
        reactionsQueue.deleteQueue()
        return getIdleEvent()
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
        val messagesPageType =
            if ((command as MessagesScreenCommand.LoadFirstPage).isMessagesListEmpty) {
                MessagesPageType.FIRST_UNREAD
            } else {
                MessagesPageType.AFTER_STORED
            }
        return loadMessages(command, messagesPageType)
    }

    private suspend fun loadPreviousPage(
        command: MessagesScreenCommand,
    ): MessagesScreenEvent.Internal {
        return loadMessages(command, MessagesPageType.OLDEST)
    }

    private suspend fun loadCurrentWithPreviousPage(
        command: MessagesScreenCommand,
    ): MessagesScreenEvent.Internal {
        return loadMessages(command, MessagesPageType.CURRENT_WITH_OLDEST)
    }

    private suspend fun loadNextPage(
        command: MessagesScreenCommand,
    ): MessagesScreenEvent.Internal {
        return loadMessages(command, MessagesPageType.NEWEST)
    }

    private suspend fun loadCurrentWithNextPage(
        command: MessagesScreenCommand,
    ): MessagesScreenEvent.Internal {
        return loadMessages(command, MessagesPageType.CURRENT_WITH_NEWEST)
    }

    private suspend fun loadLastPage(
        command: MessagesScreenCommand,
    ): MessagesScreenEvent.Internal {
        return loadMessages(command, MessagesPageType.LAST)
    }

    private suspend fun newMessageText(text: CharSequence?): MessagesScreenEvent.Internal {
        newMessageDraftState.emit(MessageDraft(messageDraft.subject, text.toString()))
        return getChangedMessageDraft()
    }

    private suspend fun newTopicName(text: CharSequence?): MessagesScreenEvent.Internal {
        newMessageDraftState.emit(MessageDraft(text.toString(), messageDraft.content))
        return getChangedMessageDraft()
    }

    private suspend fun getChangedMessageDraft(): MessagesScreenEvent.Internal {
        delay(DELAY_BEFORE_CHECK_ACTION_ICON)
        if (isMessageDraftChanged) {
            isMessageDraftChanged = false
            return MessagesScreenEvent.Internal.NewMessageDraft(messageDraft)
        }
        return getIdleEvent()
    }

    private suspend fun sendMessage(draft: MessageDraft): MessagesScreenEvent.Internal {
        if (draft.content.isNotEmpty()) {
            sendMessageUseCase(draft.subject, draft.content, messagesFilter)
                .onSuccess { messageId ->
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

    private suspend fun loadMessages(
        command: MessagesScreenCommand,
        messagesPageType: MessagesPageType,
    ): MessagesScreenEvent.Internal =
        withContext(defaultDispatcher) {
            if (isLoadingMessages.get()) return@withContext getIdleEvent()
            isLoadingMessages.set(true)
            lastLoadCommand = command
            var event: MessagesScreenEvent.Internal = MessagesScreenEvent.Internal.Idle
            getMessagesUseCase(messagesPageType, messagesFilter)
                .onSuccess { messagesResult ->
                    event = handleMessages(messagesResult, messagesPageType)
                }.onFailure { error ->
                    event = handleErrors(error)
                }
            isLoadingMessages.set(false)
            event
        }

    private suspend fun getTopics(
        command: MessagesScreenCommand.GetTopics,
    ): MessagesScreenEvent.Internal = withContext(defaultDispatcher) {
        getTopicsUseCase(command.channel).onSuccess { topicEntities ->
            val topicsNames = topicEntities.map { it.name }
            return@withContext MessagesScreenEvent.Internal.Topics(topicsNames)
        }
        getIdleEvent()
    }

    private suspend fun setMessageReadFlags(messageIds: List<Long>): MessagesScreenEvent.Internal {
        setMessagesFlagToReadUserCase(messageIds)
        return getIdleEvent()
    }

    private fun startUpdatingInfo() {
        val presencePingIntervalSeconds =
            webLimitation.getPresencePingIntervalSeconds() - DELAY_BEFORE_UPDATE_STATUS_ACTIVE * 2
        updatingInfoJob = lifecycleScope.launch {
            while (isActive) {
                if (messagesFilter.topic.name.isNotEmpty()) {
                    messagesFilter = getUpdatedMessageFilterUserCase(messagesFilter)
                }
                delay(DELAY_BEFORE_UPDATE_STATUS_ACTIVE)
                setOwnStatusActiveUseCase()
                delay(presencePingIntervalSeconds)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun subscribeToNewMessageDraftChanges() {
        newMessageDraftState
            .distinctUntilChanged()
            .debounce(DELAY_BEFORE_UPDATE_ACTION_ICON)
            .flatMapLatest { flow { emit(it) } }
            .onEach {
                messageDraft = it
                isMessageDraftChanged = true
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
        useCase: MessagesEventUseCase<T>,
        onSuccessCallback: (EventsQueueHolder, T) -> MessagesScreenEvent.Internal,
        emptyEvent: MessagesScreenEvent.Internal,
        isLastMessageVisible: Boolean,
    ): MessagesScreenEvent.Internal = withContext(defaultDispatcher) {
        if (eventsQueue.queue.queueId.isNotEmpty()) {
            useCase(eventsQueue.queue, messagesFilter, isLastMessageVisible)
                .onSuccess { event ->
                    return@withContext onSuccessCallback(eventsQueue, event)
                }
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
        editMessageUseCase(command.messageId, content = command.content.toString().trim())
            .onSuccess {
                return MessagesScreenEvent.Internal.MessageContentChanged
            }
            .onFailure {
                return handleErrors(it)
            }
        return getIdleEvent()
    }

    private suspend fun editMessageTopic(
        command: MessagesScreenCommand.EditMessageTopic,
    ): MessagesScreenEvent.Internal {
        val newTopicName = command.topic.toString().trim()
        editMessageUseCase(command.messageId, topic = newTopicName)
            .onSuccess {
                return MessagesScreenEvent.Internal.MessageTopicChanged(newTopicName)
            }
            .onFailure {
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
        val dates = TreeSet<MessageDateTime>()
        val topic = messagesFilter.topic.copy()
        forEach {
            dates.add(it.datetime)
        }
        var lastTopicName = EMPTY_STRING
        dates.forEach { messageDate ->
            messageAdapterItemList.add(DateDelegateItem(messageDate))
            var isDateChanged = true
            val allDayMessages = this.filter { message ->
                message.datetime.dateString == messageDate.dateString
            }
            allDayMessages.forEach { message ->
                if (topic.name.isEmpty() &&
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
        const val DELAY_BEFORE_RETURN_IDLE_EVENT = 3_000L
        const val DELAY_BEFORE_CHECK_EVENTS = 2_000L
        const val DELAY_BEFORE_RELOAD = 500L
        const val DELAY_BEFORE_UPDATE_STATUS_ACTIVE = 5_000L
        const val LINE_FEED = "\n"
    }
}