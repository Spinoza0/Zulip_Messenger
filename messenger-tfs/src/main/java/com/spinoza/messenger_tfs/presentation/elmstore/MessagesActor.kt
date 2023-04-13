package com.spinoza.messenger_tfs.presentation.elmstore

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.cyberfox21.tinkofffintechseminar.di.GlobalDI
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.repository.RepositoryError
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.adapter.message.date.DateDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.OwnMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.UserMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesCommand
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesEvent
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesResultDelegate
import com.spinoza.messenger_tfs.presentation.utils.EventsQueueProcessor
import com.spinoza.messenger_tfs.presentation.utils.getErrorText
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import vivid.money.elmslie.coroutines.Actor
import java.util.*

class MessagesActor(lifecycle: Lifecycle) : Actor<MessagesCommand, MessagesEvent.Internal> {

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

    override fun execute(command: MessagesCommand): Flow<MessagesEvent.Internal> = flow {
        val event = when (command) {
            is MessagesCommand.Load -> {
                messagesFilter = command.filter
                loadMessages()
            }
            is MessagesCommand.SetMessagesRead -> setMessageReadFlags(command.messageIds)
            is MessagesCommand.NewMessageText -> newMessageText(command.value)
            is MessagesCommand.UpdateReaction -> updateReaction(command.messageId, command.emoji)
            is MessagesCommand.SendMessage -> sendMessage(command.value)
            is MessagesCommand.GetMessagesEvent -> getMessagesEvent()
            is MessagesCommand.GetDeleteMessagesEvent -> getDeleteMessagesEvent()
            is MessagesCommand.GetReactionsEvent -> getReactionsEvent()
        }
        emit(event)
    }

    private suspend fun newMessageText(text: CharSequence?): MessagesEvent.Internal {
        newMessageFieldState.emit(text.toString())
        delay(DELAY_BEFORE_CHECK_ACTION_ICON)
        if (isIconActionResIdChanged) {
            isIconActionResIdChanged = false
            return MessagesEvent.Internal.IconActionResId(iconActionResId)
        }
        return MessagesEvent.Internal.Idle
    }

    private suspend fun sendMessage(value: String): MessagesEvent.Internal {
        var event: MessagesEvent.Internal = MessagesEvent.Internal.Idle
        if (value.isNotEmpty()) {
            val result = sendMessageUseCase(value, messagesFilter)
            result.onSuccess {
                isMessageSent = true
                event = MessagesEvent.Internal.MessageSent
            }
        }
        return event
    }

    private suspend fun updateReaction(messageId: Long, emoji: Emoji): MessagesEvent.Internal =
        withContext(Dispatchers.Default) {
            var event: MessagesEvent.Internal = MessagesEvent.Internal.Idle
            updateReactionUseCase(messageId, emoji, messagesFilter).onSuccess { messagesResult ->
                getOwnUserIdUseCase().onSuccess { userId ->
                    event = MessagesEvent.Internal.Messages(
                        MessagesResultDelegate(
                            messagesResult.messages.groupByDate(userId), messagesResult.position
                        )
                    )
                }.onFailure {
                    event = handleErrors(it)
                }
            }
            event
        }

    private suspend fun loadMessages(): MessagesEvent.Internal = withContext(Dispatchers.Default) {
        var event: MessagesEvent.Internal = MessagesEvent.Internal.Idle
        getMessagesUseCase(messagesFilter).onSuccess { messagesResult ->
            getOwnUserIdUseCase().onSuccess { userId ->
                event = MessagesEvent.Internal.Messages(
                    MessagesResultDelegate(
                        messagesResult.messages.groupByDate(userId), messagesResult.position
                    )
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
        }.onFailure {
            event = handleErrors(it)
        }
        event
    }

    private suspend fun setMessageReadFlags(messageIds: List<Long>): MessagesEvent.Internal {
        setMessagesFlagToReadUserCase(messageIds)
        return MessagesEvent.Internal.Idle
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

    private suspend fun getMessagesEvent(): MessagesEvent.Internal =
        withContext(Dispatchers.Default) {
            messagesQueue?.let { eventsQueue ->
                getMessageEventUseCase(eventsQueue.queue, messagesFilter).onSuccess { event ->
                    getOwnUserIdUseCase().onSuccess { userId ->
                        eventsQueue.queue = eventsQueue.queue.copy(lastEventId = event.lastEventId)
                        val messagesResult = if (isMessageSent) {
                            isMessageSent = false
                            event.messagesResult.copy(
                                position = MessagePosition(MessagePosition.Type.LAST_POSITION)
                            )
                        } else {
                            event.messagesResult
                        }
                        return@withContext MessagesEvent.Internal.MessagesEventFromQueue(
                            handleMessagesResult(messagesResult, userId)
                        )
                    }
                }
            }
            MessagesEvent.Internal.EmptyMessagesQueueEvent
        }

    private suspend fun getDeleteMessagesEvent(): MessagesEvent.Internal =
        withContext(Dispatchers.Default) {
            deleteMessagesQueue?.let { eventsQueue ->
                getDeleteMessageEventUseCase(eventsQueue.queue, messagesFilter).onSuccess { event ->
                    getOwnUserIdUseCase().onSuccess { userId ->
                        eventsQueue.queue =
                            eventsQueue.queue.copy(lastEventId = event.lastEventId)
                        return@withContext MessagesEvent.Internal.DeleteMessagesEventFromQueue(
                            handleMessagesResult(event.messagesResult, userId)
                        )
                    }
                }
            }
            MessagesEvent.Internal.EmptyDeleteMessagesQueueEvent
        }

    private suspend fun getReactionsEvent(): MessagesEvent.Internal =
        withContext(Dispatchers.Default) {
            reactionsQueue?.let { eventsQueue ->
                getReactionEventUseCase(eventsQueue.queue, messagesFilter).onSuccess { event ->
                    getOwnUserIdUseCase().onSuccess { userId ->
                        eventsQueue.queue = eventsQueue.queue.copy(lastEventId = event.lastEventId)
                        return@withContext MessagesEvent.Internal.ReactionsEventFromQueue(
                            handleMessagesResult(event.messagesResult, userId)
                        )
                    }
                }
            }
            MessagesEvent.Internal.EmptyReactionsQueueEvent
        }

    private fun handleMessagesResult(
        messagesResult: MessagesResult,
        userId: Long,
    ): MessagesResultDelegate {
        return MessagesResultDelegate(
            messagesResult.messages.groupByDate(userId), messagesResult.position
        )
    }

    private fun handleErrors(error: Throwable): MessagesEvent.Internal {
        return if (error is RepositoryError) {
            MessagesEvent.Internal.ErrorMessages(error.value)
        } else {
            MessagesEvent.Internal.ErrorNetwork(error.getErrorText())
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