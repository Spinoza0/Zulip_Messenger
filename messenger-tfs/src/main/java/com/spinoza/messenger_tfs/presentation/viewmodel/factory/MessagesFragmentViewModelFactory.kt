package com.spinoza.messenger_tfs.presentation.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.terrakok.cicerone.Router
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.viewmodel.MessagesFragmentViewModel

@Suppress("UNCHECKED_CAST")
class MessagesFragmentViewModelFactory(
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
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MessagesFragmentViewModel(
            router,
            messagesFilter,
            getOwnUserIdUseCase,
            getMessagesUseCase,
            sendMessageUseCase,
            updateReactionUseCase,
            registerEventQueueUseCase,
            deleteEventQueueUseCase,
            getMessageEventUseCase,
            getDeleteMessageEventUseCase,
            getReactionEventUseCase,
            setOwnStatusActiveUseCase,
            setMessagesFlagToReadUserCase
        ) as T
    }
}