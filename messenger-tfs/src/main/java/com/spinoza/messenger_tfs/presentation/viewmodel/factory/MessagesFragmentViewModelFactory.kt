package com.spinoza.messenger_tfs.presentation.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.viewmodel.MessagesFragmentViewModel

@Suppress("UNCHECKED_CAST")
class MessagesFragmentViewModelFactory(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val updateReactionUseCase: UpdateReactionUseCase,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MessagesFragmentViewModel(
            getMessagesUseCase,
            getUserIdUseCase,
            sendMessageUseCase,
            updateReactionUseCase
        ) as T
    }
}