package com.spinoza.messenger_tfs.presentation.feature.messages

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.model.event.DeleteMessageEvent
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.MessageEvent
import com.spinoza.messenger_tfs.domain.model.event.ReactionEvent
import com.spinoza.messenger_tfs.domain.repository.RepositoryError
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.feature.app.adapter.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.feature.app.utils.EventsQueueHolder
import com.spinoza.messenger_tfs.presentation.feature.app.utils.getErrorText
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.date.DateDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages.OwnMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages.UserMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesResultDelegate
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenCommand
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessagesScreenEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import vivid.money.elmslie.coroutines.Actor
import java.util.*
import javax.inject.Inject

class MessagesActor @Inject constructor(
    lifecycle: Lifecycle,
    private val getOwnUserIdUseCase: GetOwnUserIdUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val updateReactionUseCase: UpdateReactionUseCase,
    private val getMessageEventUseCase: GetMessageEventUseCase,
    private val getDeleteMessageEventUseCase: GetDeleteMessageEventUseCase,
    private val getReactionEventUseCase: GetReactionEventUseCase,
    private val setOwnStatusActiveUseCase: SetOwnStatusActiveUseCase,
    private val setMessagesFlagToReadUserCase: SetMessagesFlagToReadUserCase,
    registerEventQueueUseCase: RegisterEventQueueUseCase,
    deleteEventQueueUseCase: DeleteEventQueueUseCase,
) : Actor<MessagesScreenCommand, MessagesScreenEvent.Internal> {

    private lateinit var messagesFilter: MessagesFilter
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
    private var isLoadingPageWithFirstUnreadMessage = false

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
        setOwnStatusToActive()
    }

    override fun execute(command: MessagesScreenCommand): Flow<MessagesScreenEvent.Internal> =
        flow {
            val event = when (command) {
                is MessagesScreenCommand.Load -> {
                    messagesFilter = command.filter
                    loadPageWithFirstUnreadMessage(command)
                }
                is MessagesScreenCommand.LoadPreviousPage -> loadPreviousPage(command)
                is MessagesScreenCommand.LoadNextPage -> loadNextPage(command)
                is MessagesScreenCommand.LoadLastPage -> loadLastPage(command)
                is MessagesScreenCommand.SetMessagesRead -> setMessageReadFlags(command.messageIds)
                is MessagesScreenCommand.NewMessageText -> newMessageText(command.value)
                is MessagesScreenCommand.UpdateReaction ->
                    updateReaction(command.messageId, command.emoji)
                is MessagesScreenCommand.SendMessage -> sendMessage(command.value)
                is MessagesScreenCommand.GetMessagesEvent -> getMessagesEvent()
                is MessagesScreenCommand.GetDeleteMessagesEvent -> getDeleteMessagesEvent()
                is MessagesScreenCommand.GetReactionsEvent -> getReactionsEvent()
                is MessagesScreenCommand.Reload -> {
                    delay(DELAY_BEFORE_RELOAD)
                    var result: MessagesScreenEvent.Internal = MessagesScreenEvent.Internal.Idle
                    lastLoadCommand?.let { lastCommand ->
                        when (lastCommand) {
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
                            else -> result = loadPageWithFirstUnreadMessage(lastCommand)
                        }
                    }
                    result
                }
            }
            emit(event)
        }

    private suspend fun loadPageWithFirstUnreadMessage(
        command: MessagesScreenCommand,
    ): MessagesScreenEvent.Internal {
        if (isLoadingPageWithFirstUnreadMessage) return MessagesScreenEvent.Internal.Idle
        isLoadingPageWithFirstUnreadMessage = true
        lastLoadCommand = command
        val result = loadMessages(MessagesAnchor.FIRST_UNREAD)
        isLoadingPageWithFirstUnreadMessage = false
        if (result is MessagesScreenEvent.Internal.Messages) {
            registerEventQueues()
        }
        return result
    }

    private suspend fun loadPreviousPage(
        command: MessagesScreenCommand,
    ): MessagesScreenEvent.Internal {
        if (isLoadingPreviousPage) return MessagesScreenEvent.Internal.Idle
        isLoadingPreviousPage = true
        lastLoadCommand = command
        val result = loadMessages(MessagesAnchor.OLDEST)
        isLoadingPreviousPage = false
        return result
    }

    private suspend fun loadNextPage(
        command: MessagesScreenCommand,
    ): MessagesScreenEvent.Internal {
        if (isLoadingNextPage) return MessagesScreenEvent.Internal.Idle
        isLoadingNextPage = true
        lastLoadCommand = command
        val result = loadMessages(MessagesAnchor.NEWEST)
        isLoadingNextPage = false
        return result
    }

    private suspend fun loadLastPage(
        command: MessagesScreenCommand,
    ): MessagesScreenEvent.Internal {
        if (isLoadingLastPage) return MessagesScreenEvent.Internal.Idle
        isLoadingLastPage = true
        lastLoadCommand = command
        val result = loadMessages(MessagesAnchor.LAST)
        isLoadingLastPage = false
        return result
    }

    private suspend fun newMessageText(text: CharSequence?): MessagesScreenEvent.Internal {
        newMessageFieldState.emit(text.toString())
        delay(DELAY_BEFORE_CHECK_ACTION_ICON)
        if (isIconActionResIdChanged) {
            isIconActionResIdChanged = false
            return MessagesScreenEvent.Internal.IconActionResId(iconActionResId)
        }
        return MessagesScreenEvent.Internal.Idle
    }

    private suspend fun sendMessage(value: String): MessagesScreenEvent.Internal {
        var event: MessagesScreenEvent.Internal = MessagesScreenEvent.Internal.Idle
        if (value.isNotEmpty()) {
            sendMessageUseCase(value, messagesFilter).onSuccess { messagesResult ->
                event = handleMessages(messagesResult, MessagesAnchor.LAST)
            }.onFailure { error ->
                event = handleErrors(error)
            }
        }
        return event
    }

    private suspend fun updateReaction(
        messageId: Long,
        emoji: Emoji,
    ): MessagesScreenEvent.Internal =
        withContext(Dispatchers.Default) {
            var event: MessagesScreenEvent.Internal = MessagesScreenEvent.Internal.Idle
            updateReactionUseCase(messageId, emoji, messagesFilter).onSuccess { messagesResult ->
                getOwnUserIdUseCase().onSuccess { userId ->
                    event = MessagesScreenEvent.Internal.Messages(
                        MessagesResultDelegate(
                            messagesResult.messages.groupByDate(userId), messagesResult.position
                        )
                    )
                }.onFailure { error ->
                    event = handleErrors(error)
                }
            }
            event
        }

    private suspend fun loadMessages(anchor: MessagesAnchor): MessagesScreenEvent.Internal =
        withContext(Dispatchers.Default) {
            var event: MessagesScreenEvent.Internal = MessagesScreenEvent.Internal.Idle
            getMessagesUseCase(anchor, messagesFilter).onSuccess { messagesResult ->
                event = handleMessages(messagesResult, anchor)
            }.onFailure { error ->
                event = handleErrors(error)
            }
            event
        }

    private suspend fun setMessageReadFlags(messageIds: List<Long>): MessagesScreenEvent.Internal {
        setMessagesFlagToReadUserCase(messageIds)
        return MessagesScreenEvent.Internal.Idle
    }

    private fun setOwnStatusToActive() {
        lifecycleScope.launch {
            while (isActive) {
                setOwnStatusActiveUseCase()
                delay(DELAY_BEFORE_UPDATE_OWN_STATUS)
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
            .flowOn(Dispatchers.Default)
            .launchIn(lifecycleScope)
    }

    private suspend fun getMessagesEvent(): MessagesScreenEvent.Internal {
        return getEvent(
            messagesQueue, getMessageEventUseCase, ::onSuccessMessageEvent,
            MessagesScreenEvent.Internal.EmptyMessagesQueueEvent
        )
    }

    private suspend fun getDeleteMessagesEvent(): MessagesScreenEvent.Internal {
        return getEvent(
            deleteMessagesQueue, getDeleteMessageEventUseCase, ::onSuccessDeleteMessageEvent,
            MessagesScreenEvent.Internal.EmptyDeleteMessagesQueueEvent
        )
    }

    private suspend fun getReactionsEvent(): MessagesScreenEvent.Internal {
        return getEvent(
            reactionsQueue, getReactionEventUseCase, ::onSuccessReactionEvent,
            MessagesScreenEvent.Internal.EmptyReactionsQueueEvent
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
    ): MessagesScreenEvent.Internal = withContext(Dispatchers.Default) {
        if (eventsQueue.queue.queueId.isNotEmpty()) useCase(eventsQueue.queue, messagesFilter)
            .onSuccess { event ->
                getOwnUserIdUseCase().onSuccess { userId ->
                    return@withContext onSuccessCallback(eventsQueue, event, userId)
                }
            }
        delay(DELAY_BEFORE_CHECK_EVENTS)
        emptyEvent
    }

    private fun MessagesResult.toDelegate(userId: Long): MessagesResultDelegate {
        return MessagesResultDelegate(messages.groupByDate(userId), position)
    }

    private suspend fun handleMessages(
        messagesResult: MessagesResult,
        anchor: MessagesAnchor,
    ): MessagesScreenEvent.Internal {
        var event: MessagesScreenEvent.Internal = MessagesScreenEvent.Internal.Idle
        getOwnUserIdUseCase().onSuccess { userId ->
            val messagesResultDelegate = messagesResult.toDelegate(userId)
            event = if (anchor == MessagesAnchor.LAST) {
                MessagesScreenEvent.Internal.MessageSent(messagesResultDelegate)
            } else {
                MessagesScreenEvent.Internal.Messages(messagesResultDelegate)
            }
        }.onFailure {
            event = handleErrors(it)
        }
        return event
    }

    private fun registerEventQueues() {
        messagesQueue.registerQueue(listOf(EventType.MESSAGE))
        deleteMessagesQueue.registerQueue(listOf(EventType.DELETE_MESSAGE))
        reactionsQueue.registerQueue(listOf(EventType.REACTION))
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
        const val DELAY_BEFORE_CHECK_EVENTS = 1000L
        const val DELAY_BEFORE_RELOAD = 500L
        const val DELAY_BEFORE_UPDATE_OWN_STATUS = 60_000L
    }
}