package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.adapter.message.date.DateDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.OwnMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.UserMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesEffect
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesEvent
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesResultDelegate
import com.spinoza.messenger_tfs.presentation.model.messages.MessagesState
import com.spinoza.messenger_tfs.presentation.navigation.Screens
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

class MessagesFragmentViewModel(
    private val router: Router,
    private val messagesFilter: MessagesFilter,
    private val getOwnUserIdUseCase: GetOwnUserIdUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val updateReactionUseCase: UpdateReactionUseCase,
    private val registerEventQueueUseCase: RegisterEventQueueUseCase,
    private val deleteEventQueueUseCase: DeleteEventQueueUseCase,
    private val getMessageEventUseCase: GetMessageEventUseCase,
    private val getDeleteMessageEventUseCase: GetDeleteMessageEventUseCase,
    private val getReactionEventUseCase: GetReactionEventUseCase,
    private val setOwnStatusActiveUseCase: SetOwnStatusActiveUseCase,
    private val setMessagesFlagToReadUserCase: SetMessagesFlagToReadUserCase,
) : ViewModel() {

    val state: StateFlow<MessagesState>
        get() = _state.asStateFlow()
    val effects: SharedFlow<MessagesEffect>
        get() = _effects.asSharedFlow()

    private val _state = MutableStateFlow(MessagesState())
    private val _effects = MutableSharedFlow<MessagesEffect>()
    private val newMessageFieldState = MutableSharedFlow<String>()
    private var messagesQueue = EventsQueue()
    private var deleteMessagesQueue = EventsQueue()
    private var reactionsQueue = EventsQueue()
    private var isMessageSent = false
    private var isGoingBack = false

    init {
        loadMessages()
        subscribeToNewMessageFieldChanges()
        setOwnStatusToActive()
    }

    fun reduce(event: MessagesEvent) {
        when (event) {
            is MessagesEvent.Ui.SendMessage -> sendMessage(event.value.toString())
            is MessagesEvent.Ui.UpdateReaction -> updateReaction(event.messageId, event.emoji)
            is MessagesEvent.Ui.NewMessageText -> doOnTextChanged(event.value)
            is MessagesEvent.Ui.SetMessagesRead -> setMessageReadFlags(event.messageIds)
            is MessagesEvent.Ui.Load -> loadMessages()
            is MessagesEvent.Ui.Exit -> goBack()
            is MessagesEvent.Ui.ShowUserInfo ->
                router.navigateTo(Screens.UserProfile(event.message.userId))
            is MessagesEvent.Ui.AfterSubmitMessages -> state.value.messages?.let { messages ->
                _state.value = (state.value.copy(
                    messages = messages.copy(
                        position = messages.position.copy(type = MessagePosition.Type.UNDEFINED)
                    )
                ))
            }
        }
    }

    private fun goBack() {
        if (!isGoingBack) {
            isGoingBack = true
            router.exit()
        }
    }

    private fun sendMessage(value: String) {
        if (value.isNotEmpty()) viewModelScope.launch {
            _state.emit(state.value.copy(isSendingMessage = true))
            val result = sendMessageUseCase(value, messagesFilter)
            _state.emit(state.value.copy(isSendingMessage = false))
            if (result is RepositoryResult.Success) {
                isMessageSent = true
                _effects.emit(MessagesEffect.MessageSent)
            }
        }
    }

    private fun updateReaction(messageId: Long, emoji: Emoji) {
        viewModelScope.launch {
            val result = updateReactionUseCase(messageId, emoji, messagesFilter)
            if (result is RepositoryResult.Success) {
                when (val userIdResult = getOwnUserIdUseCase()) {
                    is RepositoryResult.Success ->
                        handleMessagesResult(result.value, userIdResult.value)
                    is RepositoryResult.Failure -> handleErrors(userIdResult)
                }
            }
        }
    }

    private fun doOnTextChanged(text: CharSequence?) {
        viewModelScope.launch {
            newMessageFieldState.emit(text.toString())
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _state.emit(state.value.copy(isLoading = true))
            val result = getMessagesUseCase(messagesFilter)
            _state.emit(state.value.copy(isLoading = false))
            handleRepositoryResult(result)
        }
    }

    private fun setMessageReadFlags(messageIds: List<Long>) {
        viewModelScope.launch {
            setMessagesFlagToReadUserCase(messageIds)
        }
    }

    private fun setOwnStatusToActive() {
        viewModelScope.launch {
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
                _effects.emit(MessagesEffect.UpdateIconImage(resId))
            }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    private suspend fun handleRepositoryResult(result: RepositoryResult<MessagesResult>) {
        when (result) {
            is RepositoryResult.Success -> withContext(Dispatchers.Default) {
                when (val userIdResult = getOwnUserIdUseCase()) {
                    is RepositoryResult.Success -> {
                        handleMessagesResult(result.value, userIdResult.value)
                        registerMessagesQueue()
                        registerDeleteMessagesQueue()
                        registerReactionsQueue()
                    }
                    is RepositoryResult.Failure -> handleErrors(userIdResult)
                }
            }
            is RepositoryResult.Failure -> handleErrors(result)
        }
    }

    private suspend fun handleMessagesResult(result: MessagesResult, userId: Long) {
        _state.emit(
            state.value.copy(
                messages =
                MessagesResultDelegate(result.messages.groupByDate(userId), result.position)
            )
        )
    }

    private suspend fun registerMessagesQueue() {
        when (val queueResult =
            registerEventQueueUseCase(listOf(EventType.MESSAGE), messagesFilter)) {
            is RepositoryResult.Success -> {
                messagesQueue = queueResult.value
                checkMessagesEvents()
            }
            is RepositoryResult.Failure -> handleErrors(queueResult)
        }
    }

    private suspend fun registerDeleteMessagesQueue() {
        when (val queueResult =
            registerEventQueueUseCase(listOf(EventType.DELETE_MESSAGE), messagesFilter)) {
            is RepositoryResult.Success -> {
                deleteMessagesQueue = queueResult.value
                checkDeleteMessagesEvents()
            }
            is RepositoryResult.Failure -> handleErrors(queueResult)
        }
    }

    private suspend fun registerReactionsQueue() {
        when (val queueResult =
            registerEventQueueUseCase(listOf(EventType.REACTION), messagesFilter)) {
            is RepositoryResult.Success -> {
                reactionsQueue = queueResult.value
                checkReactionsEvents()
            }
            is RepositoryResult.Failure -> handleErrors(queueResult)
        }
    }

    private fun checkMessagesEvents() {
        viewModelScope.launch {
            while (true) {
                val eventResult =
                    getMessageEventUseCase(messagesQueue, messagesFilter)
                if (eventResult is RepositoryResult.Success) {
                    val userIdResult = getOwnUserIdUseCase()
                    if (userIdResult is RepositoryResult.Success) {
                        messagesQueue =
                            messagesQueue.copy(lastEventId = eventResult.value.lastEventId)
                        val messagesResult = if (isMessageSent) {
                            isMessageSent = false
                            eventResult.value.messagesResult.copy(
                                position = MessagePosition(MessagePosition.Type.LAST_POSITION)
                            )
                        } else {
                            eventResult.value.messagesResult
                        }
                        handleMessagesResult(messagesResult, userIdResult.value)
                    }
                }
            }
        }
    }

    private fun checkDeleteMessagesEvents() {
        viewModelScope.launch {
            while (true) {
                val eventResult =
                    getDeleteMessageEventUseCase(deleteMessagesQueue, messagesFilter)
                if (eventResult is RepositoryResult.Success) {
                    val userIdResult = getOwnUserIdUseCase()
                    if (userIdResult is RepositoryResult.Success) {
                        deleteMessagesQueue =
                            deleteMessagesQueue.copy(lastEventId = eventResult.value.lastEventId)
                        handleMessagesResult(eventResult.value.messagesResult, userIdResult.value)
                    }
                }
                delay(DELAY_DELETE_MESSAGES_EVENTS)
            }
        }
    }

    private fun checkReactionsEvents() {
        viewModelScope.launch {
            while (true) {
                val eventResult =
                    getReactionEventUseCase(reactionsQueue, messagesFilter)
                if (eventResult is RepositoryResult.Success) {
                    val userIdResult = getOwnUserIdUseCase()
                    if (userIdResult is RepositoryResult.Success) {
                        reactionsQueue =
                            reactionsQueue.copy(lastEventId = eventResult.value.lastEventId)
                        handleMessagesResult(eventResult.value.messagesResult, userIdResult.value)
                    }
                }
                delay(DELAY_REACTIONS_EVENTS)
            }
        }
    }

    private suspend fun handleErrors(error: RepositoryResult.Failure) {
        when (error) {
            is RepositoryResult.Failure.OwnUserNotFound ->
                _effects.emit(MessagesEffect.Failure.OwnUserNotFound(error.value))
            is RepositoryResult.Failure.MessageNotFound ->
                _effects.emit(MessagesEffect.Failure.MessageNotFound(error.messageId))
            is RepositoryResult.Failure.UserNotFound ->
                _effects.emit(MessagesEffect.Failure.UserNotFound(error.userId, error.value))
            is RepositoryResult.Failure.SendingMessage ->
                _effects.emit(MessagesEffect.Failure.SendingMessage(error.value))
            is RepositoryResult.Failure.UpdatingReaction ->
                _effects.emit(MessagesEffect.Failure.UpdatingReaction(error.value))
            is RepositoryResult.Failure.Network ->
                _effects.emit(MessagesEffect.Failure.Network(error.value))
            is RepositoryResult.Failure.LoadingMessages -> _effects.emit(
                MessagesEffect.Failure.LoadingMessages(error.messagesFilter, error.value)
            )
            else -> {}
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

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            deleteEventQueueUseCase(messagesQueue.queueId)
            deleteEventQueueUseCase(deleteMessagesQueue.queueId)
            deleteEventQueueUseCase(reactionsQueue.queueId)
        }
    }

    private companion object {

        const val DELAY_BEFORE_UPDATE_ACTION_ICON = 200L
        const val DELAY_BEFORE_UPDATE_OWN_STATUS = 60_000L
        const val DELAY_REACTIONS_EVENTS = 200L
        const val DELAY_DELETE_MESSAGES_EVENTS = 500L
    }
}