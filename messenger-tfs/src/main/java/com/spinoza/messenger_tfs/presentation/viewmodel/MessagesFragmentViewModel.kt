package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.repository.RepositoryError
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.adapter.message.date.DateDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.OwnMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.UserMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.getErrorText
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
            result.onSuccess {
                isMessageSent = true
                _effects.emit(MessagesEffect.MessageSent)
            }
        }
    }

    private fun updateReaction(messageId: Long, emoji: Emoji) {
        viewModelScope.launch {
            updateReactionUseCase(messageId, emoji, messagesFilter).onSuccess { messagesResult ->
                getOwnUserIdUseCase().onSuccess {
                    handleMessagesResult(messagesResult, it)
                }.onFailure {
                    handleErrors(it)
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
            result.onSuccess { messagesResult ->
                getOwnUserIdUseCase().onSuccess {
                    handleMessagesResult(messagesResult, it)
                    registerMessagesQueue()
                    registerDeleteMessagesQueue()
                    registerReactionsQueue()
                }.onFailure {
                    handleErrors(it)
                }
            }.onFailure {
                handleErrors(it)
            }
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
                _state.emit(state.value.copy(iconActionResId = resId))
            }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    private suspend fun handleMessagesResult(result: MessagesResult, userId: Long) =
        withContext(Dispatchers.Default) {
            _state.emit(
                state.value.copy(
                    messages =
                    MessagesResultDelegate(result.messages.groupByDate(userId), result.position)
                )
            )
        }

    private suspend fun registerMessagesQueue() {
        var isRegistrationSuccess = false
        while (!isRegistrationSuccess) {
            registerEventQueueUseCase(listOf(EventType.MESSAGE), messagesFilter).onSuccess {
                messagesQueue = it
                checkMessagesEvents()
                isRegistrationSuccess = true
            }.onFailure {
                delay(DELAY_BEFORE_REGISTRATION_ATTEMPT)
            }
        }
    }

    private suspend fun registerDeleteMessagesQueue() {
        var isRegistrationSuccess = false
        while (!isRegistrationSuccess) {
            registerEventQueueUseCase(listOf(EventType.DELETE_MESSAGE), messagesFilter).onSuccess {
                deleteMessagesQueue = it
                checkDeleteMessagesEvents()
                isRegistrationSuccess = true
            }.onFailure {
                delay(DELAY_BEFORE_REGISTRATION_ATTEMPT)
            }
        }
    }

    private suspend fun registerReactionsQueue() {
        var isRegistrationSuccess = false
        while (!isRegistrationSuccess) {
            registerEventQueueUseCase(listOf(EventType.REACTION), messagesFilter).onSuccess {
                reactionsQueue = it
                checkReactionsEvents()
                isRegistrationSuccess = true
            }.onFailure {
                delay(DELAY_BEFORE_REGISTRATION_ATTEMPT)
            }
        }
    }

    private fun checkMessagesEvents() {
        viewModelScope.launch {
            while (true) {
                getMessageEventUseCase(messagesQueue, messagesFilter).onSuccess { event ->
                    getOwnUserIdUseCase().onSuccess { userId ->
                        messagesQueue = messagesQueue.copy(lastEventId = event.lastEventId)
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
        viewModelScope.launch {
            while (true) {
                getDeleteMessageEventUseCase(deleteMessagesQueue, messagesFilter)
                    .onSuccess { event ->
                        getOwnUserIdUseCase().onSuccess { userId ->
                            deleteMessagesQueue =
                                deleteMessagesQueue.copy(lastEventId = event.lastEventId)
                            handleMessagesResult(event.messagesResult, userId)
                        }
                    }
                delay(DELAY_DELETE_MESSAGES_EVENTS)
            }
        }
    }

    private fun checkReactionsEvents() {
        viewModelScope.launch {
            while (true) {
                getReactionEventUseCase(reactionsQueue, messagesFilter).onSuccess { event ->
                    getOwnUserIdUseCase().onSuccess { userId ->
                        reactionsQueue = reactionsQueue.copy(lastEventId = event.lastEventId)
                        handleMessagesResult(event.messagesResult, userId)
                    }
                }
                delay(DELAY_REACTIONS_EVENTS)
            }
        }
    }

    private suspend fun handleErrors(error: Throwable) {
        val messagesEffect = if (error is RepositoryError) {
            MessagesEffect.Failure.Error(error.value)
        } else {
            MessagesEffect.Failure.Network(error.getErrorText())
        }
        _effects.emit(messagesEffect)
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
        const val DELAY_BEFORE_REGISTRATION_ATTEMPT = 10_000L
        const val DELAY_REACTIONS_EVENTS = 200L
        const val DELAY_DELETE_MESSAGES_EVENTS = 500L
    }
}