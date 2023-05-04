package com.spinoza.messenger_tfs.presentation.feature.messages

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
import com.spinoza.messenger_tfs.domain.usecase.event.DeleteEventQueueUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.EventUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.GetDeleteMessageEventUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.GetMessageEventUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.GetReactionEventUseCase
import com.spinoza.messenger_tfs.domain.usecase.event.RegisterEventQueueUseCase
import com.spinoza.messenger_tfs.domain.usecase.messages.GetMessagesUseCase
import com.spinoza.messenger_tfs.domain.usecase.messages.GetOwnUserIdUseCase
import com.spinoza.messenger_tfs.domain.usecase.messages.GetStoredMessagesUseCase
import com.spinoza.messenger_tfs.domain.usecase.messages.GetUpdatedMessageFilterUserCase
import com.spinoza.messenger_tfs.domain.usecase.messages.SaveAttachmentsUseCase
import com.spinoza.messenger_tfs.domain.usecase.messages.SendMessageUseCase
import com.spinoza.messenger_tfs.domain.usecase.messages.SetMessagesFlagToReadUserCase
import com.spinoza.messenger_tfs.domain.usecase.messages.SetOwnStatusActiveUseCase
import com.spinoza.messenger_tfs.domain.usecase.messages.UpdateReactionUseCase
import com.spinoza.messenger_tfs.domain.usecase.messages.UploadFileUseCase
import com.spinoza.messenger_tfs.presentation.adapter.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.date.DateDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages.OwnMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages.UserMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesResultDelegate
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenEvent
import com.spinoza.messenger_tfs.presentation.util.EventsQueueHolder
import com.spinoza.messenger_tfs.presentation.util.getErrorText
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
    private val getOwnUserIdUseCase: GetOwnUserIdUseCase,
    private val getStoredMessagesUseCase: GetStoredMessagesUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val updateReactionUseCase: UpdateReactionUseCase,
    private val getMessageEventUseCase: GetMessageEventUseCase,
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
                is MessagesScreenCommand.SaveAttachments -> saveAttachments(command)
            }
            emit(event)
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

    private suspend fun updateReaction(
        messageId: Long,
        emoji: Emoji,
    ): MessagesScreenEvent.Internal =
        withContext(defaultDispatcher) {
            updateReactionUseCase(messageId, emoji, messagesFilter).onSuccess { messagesResult ->
                getOwnUserIdUseCase().onSuccess { userId ->
                    return@withContext MessagesScreenEvent.Internal.Messages(
                        messagesResult.toDelegate(userId)
                    )
                }.onFailure { error ->
                    return@withContext handleErrors(error)
                }
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
        userId: Long,
    ): MessagesScreenEvent.Internal {
        updateLastEventId(eventsQueue, event.lastEventId)
        return MessagesScreenEvent.Internal.MessagesEventFromQueue(
            event.messagesResult.toDelegate(userId)
        )
    }

    private fun onSuccessDeleteMessageEvent(
        eventsQueue: EventsQueueHolder,
        event: DeleteMessageEvent,
        userId: Long,
    ): MessagesScreenEvent.Internal {
        updateLastEventId(eventsQueue, event.lastEventId)
        return MessagesScreenEvent.Internal.DeleteMessagesEventFromQueue(
            event.messagesResult.toDelegate(userId)
        )
    }

    private fun onSuccessReactionEvent(
        eventsQueue: EventsQueueHolder,
        event: ReactionEvent,
        userId: Long,
    ): MessagesScreenEvent.Internal {
        updateLastEventId(eventsQueue, event.lastEventId)
        return MessagesScreenEvent.Internal.ReactionsEventFromQueue(
            event.messagesResult.toDelegate(userId)
        )
    }

    private fun updateLastEventId(eventsQueue: EventsQueueHolder, lastEventId: Long) {
        eventsQueue.queue = eventsQueue.queue.copy(lastEventId = lastEventId)
    }

    private suspend fun <T> getEvent(
        eventsQueue: EventsQueueHolder,
        useCase: EventUseCase<T>,
        onSuccessCallback: (EventsQueueHolder, T, Long) -> MessagesScreenEvent.Internal,
        emptyEvent: MessagesScreenEvent.Internal,
        isLastMessageVisible: Boolean,
    ): MessagesScreenEvent.Internal = withContext(defaultDispatcher) {
        if (eventsQueue.queue.queueId.isNotEmpty()) useCase(
            eventsQueue.queue,
            messagesFilter,
            isLastMessageVisible
        ).onSuccess { event ->
            getOwnUserIdUseCase().onSuccess { userId ->
                return@withContext onSuccessCallback(eventsQueue, event, userId)
            }
        }
        delay(DELAY_BEFORE_CHECK_EVENTS)
        emptyEvent
    }

    private fun MessagesResult.toDelegate(userId: Long): MessagesResultDelegate {
        return MessagesResultDelegate(messages.groupByDate(userId), position, isNewMessageExisting)
    }

    private suspend fun handleMessages(
        messagesResult: MessagesResult,
        messagesPageType: MessagesPageType,
    ): MessagesScreenEvent.Internal {
        getOwnUserIdUseCase().onSuccess { userId ->
            val messagesResultDelegate = messagesResult.toDelegate(userId)
            if (messagesPageType == MessagesPageType.STORED) {
                return MessagesScreenEvent.Internal.StoredMessages(messagesResultDelegate)
            }
            return MessagesScreenEvent.Internal.Messages(messagesResultDelegate)
        }.onFailure {
            return handleErrors(it)
        }
        return getIdleEvent()
    }

    private fun registerEventQueues() {
        messagesQueue.registerQueue(listOf(EventType.MESSAGE))
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
            MessagesScreenEvent.Internal.ErrorNetwork(error.getErrorText())
        }
    }

    private fun List<Message>.groupByDate(userId: Long): List<DelegateAdapterItem> {
        val messageAdapterItemList = mutableListOf<DelegateAdapterItem>()
        val dates = TreeSet<MessageDate>()
        forEach {
            dates.add(it.date)
        }
        dates.forEach { messageDate ->
            messageAdapterItemList.add(DateDelegateItem(messageDate))
            val allDayMessages = this.filter { message ->
                message.date.value == messageDate.value
            }
            allDayMessages.forEach { message ->
                if (message.user.userId == userId) {
                    messageAdapterItemList.add(OwnMessageDelegateItem(message))
                } else {
                    messageAdapterItemList.add(UserMessageDelegateItem(message))
                }
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
    }
}