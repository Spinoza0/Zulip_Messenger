package com.spinoza.messenger_tfs.presentation.elm

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
    private val registerEventQueueUseCase = GlobalDI.INSTANCE.registerEventQueueUseCase
    private val deleteEventQueueUseCase = GlobalDI.INSTANCE.deleteEventQueueUseCase
    private val getMessageEventUseCase = GlobalDI.INSTANCE.getMessageEventUseCase
    private val getDeleteMessageEventUseCase = GlobalDI.INSTANCE.getDeleteMessageEventUseCase
    private val getReactionEventUseCase = GlobalDI.INSTANCE.getReactionEventUseCase
    private val setOwnStatusActiveUseCase = GlobalDI.INSTANCE.setOwnStatusActiveUseCase
    private val setMessagesFlagToReadUserCase = GlobalDI.INSTANCE.setMessagesFlagToReadUserCase

    private val actorFlow = MutableSharedFlow<MessagesEvent.Internal>()

    private val newMessageFieldState = MutableSharedFlow<String>()
    private lateinit var messagesFilter: MessagesFilter
    private lateinit var messagesQueue: EventsQueueProcessor
    private lateinit var deleteMessagesQueue: EventsQueueProcessor
    private lateinit var reactionsQueue: EventsQueueProcessor
    private var isMessageSent = false

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            messagesQueue.deleteQueue()
            deleteMessagesQueue.deleteQueue()
            reactionsQueue.deleteQueue()
        }
    }

    init {
        lifecycle.addObserver(lifecycleObserver)
        subscribeToNewMessageFieldChanges()
        setOwnStatusToActive()
    }

    override fun execute(command: MessagesCommand): Flow<MessagesEvent.Internal> {
        when (command) {
            is MessagesCommand.Load -> {
                messagesFilter = command.filter
                messagesQueue = EventsQueueProcessor(
                    registerEventQueueUseCase,
                    deleteEventQueueUseCase,
                    messagesFilter
                )
                deleteMessagesQueue = EventsQueueProcessor(
                    registerEventQueueUseCase,
                    deleteEventQueueUseCase,
                    messagesFilter
                )
                reactionsQueue = EventsQueueProcessor(
                    registerEventQueueUseCase,
                    deleteEventQueueUseCase,
                    messagesFilter
                )
                loadMessages()
            }
            is MessagesCommand.SetMessagesRead -> setMessageReadFlags(command.messageIds)
            is MessagesCommand.NewMessageText -> newMessageText(command.value)
            is MessagesCommand.UpdateReaction -> updateReaction(command.messageId, command.emoji)
            is MessagesCommand.SendMessage -> sendMessage(command.value.toString())

        }
        return actorFlow.asSharedFlow()
    }

    private fun newMessageText(text: CharSequence?) {
        lifecycleScope.launch {
            newMessageFieldState.emit(text.toString())
        }
    }

    private fun sendMessage(value: String) {
        if (value.isNotEmpty()) lifecycleScope.launch {
            val result = sendMessageUseCase(value, messagesFilter)
            result.onSuccess {
                isMessageSent = true
                actorFlow.emit(MessagesEvent.Internal.MessageSent)
            }
        }
    }

    private fun updateReaction(messageId: Long, emoji: Emoji) {
        lifecycleScope.launch {
            updateReactionUseCase(messageId, emoji, messagesFilter).onSuccess { messagesResult ->
                getOwnUserIdUseCase().onSuccess { userId ->
                    handleMessagesResult(messagesResult, userId)
                }.onFailure {
                    handleErrors(it)
                }
            }
        }
    }

    private fun loadMessages() {
        lifecycleScope.launch {
            getMessagesUseCase(messagesFilter).onSuccess { messagesResult ->
                getOwnUserIdUseCase().onSuccess { userId ->
                    handleMessagesResult(messagesResult, userId)
                    messagesQueue.registerQueue(EventType.MESSAGE, ::checkMessagesEvents)
                    deleteMessagesQueue.registerQueue(
                        EventType.DELETE_MESSAGE,
                        ::checkDeleteMessagesEvents
                    )
                    reactionsQueue.registerQueue(EventType.REACTION, ::checkReactionsEvents)
                }.onFailure {
                    handleErrors(it)
                }
            }.onFailure {
                handleErrors(it)
            }
        }
    }

    private fun setMessageReadFlags(messageIds: List<Long>) {
        lifecycleScope.launch {
            setMessagesFlagToReadUserCase(messageIds)
        }
    }

    private fun setOwnStatusToActive() {
        lifecycleScope.launch {
            while (true) {
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
                actorFlow.emit(MessagesEvent.Internal.IconActionResId(resId))
            }
            .flowOn(Dispatchers.Default)
            .launchIn(lifecycleScope)
    }

    private suspend fun handleMessagesResult(result: MessagesResult, userId: Long) =
        withContext(Dispatchers.Default) {
            actorFlow.emit(
                MessagesEvent.Internal.Messages(
                    MessagesResultDelegate(
                        result.messages.groupByDate(userId), result.position
                    )
                )
            )
        }

    private fun checkMessagesEvents() {
        lifecycleScope.launch {
            while (true) {
                getMessageEventUseCase(messagesQueue.queue, messagesFilter).onSuccess { event ->
                    getOwnUserIdUseCase().onSuccess { userId ->
                        messagesQueue.queue =
                            messagesQueue.queue.copy(lastEventId = event.lastEventId)
                        val messagesResult = if (isMessageSent) {
                            isMessageSent = false
                            event.messagesResult.copy(
                                position = MessagePosition(MessagePosition.Type.LAST_POSITION)
                            )
                        } else {
                            event.messagesResult
                        }
                        handleMessagesResult(messagesResult, userId)
                    }
                }
            }
        }
    }

    private fun checkDeleteMessagesEvents() {
        lifecycleScope.launch {
            while (true) {
                getDeleteMessageEventUseCase(deleteMessagesQueue.queue, messagesFilter)
                    .onSuccess { event ->
                        getOwnUserIdUseCase().onSuccess { userId ->
                            deleteMessagesQueue.queue =
                                deleteMessagesQueue.queue.copy(lastEventId = event.lastEventId)
                            handleMessagesResult(event.messagesResult, userId)
                        }
                    }
                delay(DELAY_DELETE_MESSAGES_EVENTS)
            }
        }
    }

    private fun checkReactionsEvents() {
        lifecycleScope.launch {
            while (true) {
                getReactionEventUseCase(reactionsQueue.queue, messagesFilter).onSuccess { event ->
                    getOwnUserIdUseCase().onSuccess { userId ->
                        reactionsQueue.queue =
                            reactionsQueue.queue.copy(lastEventId = event.lastEventId)
                        handleMessagesResult(event.messagesResult, userId)
                    }
                }
                delay(DELAY_REACTIONS_EVENTS)
            }
        }
    }

    private suspend fun handleErrors(error: Throwable) {
        val messagesEvent = if (error is RepositoryError) {
            MessagesEvent.Internal.ErrorMessages(error.value)
        } else {
            MessagesEvent.Internal.ErrorNetwork(error.getErrorText())
        }
        actorFlow.emit(messagesEvent)
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
        const val DELAY_BEFORE_UPDATE_OWN_STATUS = 60_000L
        const val DELAY_REACTIONS_EVENTS = 200L
        const val DELAY_DELETE_MESSAGES_EVENTS = 500L
    }
}