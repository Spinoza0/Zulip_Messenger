package com.spinoza.messenger_tfs.presentation.elmstore

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.di.GlobalDI
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.model.event.DeleteMessageEvent
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.MessageEvent
import com.spinoza.messenger_tfs.domain.model.event.ReactionEvent
import com.spinoza.messenger_tfs.domain.repository.RepositoryError
import com.spinoza.messenger_tfs.domain.usecase.EventUseCase
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.adapter.message.date.DateDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.OwnMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.UserMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesResultDelegate
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesScreenCommand
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesScreenEvent
import com.spinoza.messenger_tfs.presentation.utils.EventsQueueProcessor
import com.spinoza.messenger_tfs.presentation.utils.getErrorText
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import vivid.money.elmslie.coroutines.Actor
import java.util.*

class MessagesActor(
    lifecycle: Lifecycle,
) : Actor<MessagesScreenCommand, MessagesScreenEvent.Internal> {

    private val lifecycleScope = lifecycle.coroutineScope
    private val getOwnUserIdUseCase = GlobalDI.INSTANCE.getOwnUserIdUseCase
    private val getMessagesUseCase = GlobalDI.INSTANCE.getMessagesUseCase
    private val sendMessageUseCase = GlobalDI.INSTANCE.sendMessageUseCase
    private val updateReactionUseCase = GlobalDI.INSTANCE.updateReactionUseCase
    private val getMessageEventUseCase = GlobalDI.INSTANCE.getMessageEventUseCase
    private val getDeleteMessageEventUseCase = GlobalDI.INSTANCE.getDeleteMessageEventUseCase
    private val getReactionEventUseCase = GlobalDI.INSTANCE.getReactionEventUseCase
    private val setOwnStatusActiveUseCase = GlobalDI.INSTANCE.setOwnStatusActiveUseCase
    private val setMessagesFlagToReadUserCase = GlobalDI.INSTANCE.setMessagesFlagToReadUserCase

    private val newMessageFieldState = MutableSharedFlow<String>()
    private lateinit var messagesFilter: MessagesFilter
    private var messagesQueue: EventsQueueProcessor? = null
    private var deleteMessagesQueue: EventsQueueProcessor? = null
    private var reactionsQueue: EventsQueueProcessor? = null
    private var isMessageSent = false

    private var iconActionResId = R.drawable.ic_add_circle_outline
    private var isIconActionResIdChanged = false

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            messagesQueue?.deleteQueue()
            deleteMessagesQueue?.deleteQueue()
            reactionsQueue?.deleteQueue()
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
                    loadMessages()
                }
                is MessagesScreenCommand.SetMessagesRead -> setMessageReadFlags(command.messageIds)
                is MessagesScreenCommand.NewMessageText -> newMessageText(command.value)
                is MessagesScreenCommand.UpdateReaction -> updateReaction(
                    command.messageId,
                    command.emoji
                )
                is MessagesScreenCommand.SendMessage -> sendMessage(command.value)
                is MessagesScreenCommand.GetMessagesEvent -> getMessagesEvent()
                is MessagesScreenCommand.GetDeleteMessagesEvent -> getDeleteMessagesEvent()
                is MessagesScreenCommand.GetReactionsEvent -> getReactionsEvent()
            }
            emit(event)
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
            val result = sendMessageUseCase(value, messagesFilter)
            result.onSuccess {
                isMessageSent = true
                event = MessagesScreenEvent.Internal.MessageSent
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

    private suspend fun loadMessages(): MessagesScreenEvent.Internal =
        withContext(Dispatchers.Default) {
            var event: MessagesScreenEvent.Internal = MessagesScreenEvent.Internal.Idle
            getMessagesUseCase(messagesFilter).onSuccess { messagesResult ->
                event = handleMessages(messagesResult)
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
        messagesQueue?.let { queue ->
            return getEvent(
                queue, getMessageEventUseCase, ::onSuccessMessageEvent,
                MessagesScreenEvent.Internal.EmptyMessagesQueueEvent
            )
        }
        return MessagesScreenEvent.Internal.EmptyMessagesQueueEvent
    }

    private suspend fun getDeleteMessagesEvent(): MessagesScreenEvent.Internal {
        deleteMessagesQueue?.let { queue ->
            return getEvent(
                queue, getDeleteMessageEventUseCase, ::onSuccessDeleteMessageEvent,
                MessagesScreenEvent.Internal.EmptyDeleteMessagesQueueEvent
            )
        }
        return MessagesScreenEvent.Internal.EmptyDeleteMessagesQueueEvent
    }

    private suspend fun getReactionsEvent(): MessagesScreenEvent.Internal {
        reactionsQueue?.let { queue ->
            return getEvent(
                queue, getReactionEventUseCase, ::onSuccessReactionEvent,
                MessagesScreenEvent.Internal.EmptyReactionsQueueEvent
            )
        }
        return MessagesScreenEvent.Internal.EmptyReactionsQueueEvent
    }

    private fun onSuccessMessageEvent(
        eventsQueue: EventsQueueProcessor,
        event: MessageEvent,
        userId: Long,
    ): MessagesScreenEvent.Internal {
        updateLastEventId(eventsQueue, event.lastEventId)
        val messagesResult = if (isMessageSent) {
            isMessageSent = false
            event.messagesResult.copy(position = MessagePosition(MessagePosition.Type.LAST_POSITION))
        } else {
            event.messagesResult
        }
        return MessagesScreenEvent.Internal.MessagesEventFromQueue(
            getMessagesResultDelegate(messagesResult, userId)
        )
    }

    private fun onSuccessDeleteMessageEvent(
        eventsQueue: EventsQueueProcessor,
        event: DeleteMessageEvent,
        userId: Long,
    ): MessagesScreenEvent.Internal {
        updateLastEventId(eventsQueue, event.lastEventId)
        return MessagesScreenEvent.Internal.DeleteMessagesEventFromQueue(
            getMessagesResultDelegate(event.messagesResult, userId)
        )
    }

    private fun onSuccessReactionEvent(
        eventsQueue: EventsQueueProcessor,
        event: ReactionEvent,
        userId: Long,
    ): MessagesScreenEvent.Internal {
        updateLastEventId(eventsQueue, event.lastEventId)
        return MessagesScreenEvent.Internal.ReactionsEventFromQueue(
            getMessagesResultDelegate(event.messagesResult, userId)
        )
    }

    private fun updateLastEventId(eventsQueue: EventsQueueProcessor, lastEventId: Long) {
        eventsQueue.queue = eventsQueue.queue.copy(lastEventId = lastEventId)
    }

    private suspend fun <T> getEvent(
        eventsQueue: EventsQueueProcessor,
        useCase: EventUseCase<T>,
        onSuccessCallback: (EventsQueueProcessor, T, Long) -> MessagesScreenEvent.Internal,
        emptyEvent: MessagesScreenEvent.Internal,
    ): MessagesScreenEvent.Internal = withContext(Dispatchers.Default) {
        useCase(eventsQueue.queue, messagesFilter).onSuccess { event ->
            getOwnUserIdUseCase().onSuccess { userId ->
                return@withContext onSuccessCallback(eventsQueue, event, userId)
            }
        }
        emptyEvent
    }

    private fun getMessagesResultDelegate(
        messagesResult: MessagesResult,
        userId: Long,
    ): MessagesResultDelegate {
        return MessagesResultDelegate(
            messagesResult.messages.groupByDate(userId), messagesResult.position
        )
    }

    private suspend fun handleMessages(messagesResult: MessagesResult): MessagesScreenEvent.Internal {
        var event: MessagesScreenEvent.Internal = MessagesScreenEvent.Internal.Idle
        getOwnUserIdUseCase().onSuccess { userId ->
            event = MessagesScreenEvent.Internal.Messages(
                getMessagesResultDelegate(messagesResult, userId)
            )
            messagesQueue = EventsQueueProcessor(lifecycleScope, messagesFilter).apply {
                registerQueue(EventType.MESSAGE)
            }
            deleteMessagesQueue = EventsQueueProcessor(lifecycleScope, messagesFilter).apply {
                registerQueue(EventType.DELETE_MESSAGE)
            }
            reactionsQueue = EventsQueueProcessor(lifecycleScope, messagesFilter).apply {
                registerQueue(EventType.REACTION)
            }
        }.onFailure {
            event = handleErrors(it)
        }
        return event
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
        const val DELAY_BEFORE_UPDATE_OWN_STATUS = 60_000L
    }
}