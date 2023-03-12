package com.spinoza.messenger_tfs.presentation.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spinoza.messenger_tfs.domain.usecase.GetMessagesStateUseCase
import com.spinoza.messenger_tfs.domain.usecase.LoadMessagesUseCase
import com.spinoza.messenger_tfs.domain.usecase.SendMessageUseCase
import com.spinoza.messenger_tfs.domain.usecase.UpdateReactionUseCase
import com.spinoza.messenger_tfs.presentation.viewmodel.MessagesFragmentViewModel

class MessageFragmentViewModelFactory(
    private val getMessagesStateUseCase: GetMessagesStateUseCase,
    private val loadMessagesUseCase: LoadMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val updateReactionUseCase: UpdateReactionUseCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessagesFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MessagesFragmentViewModel(
                getMessagesStateUseCase,
                loadMessagesUseCase,
                sendMessageUseCase,
                updateReactionUseCase
            ) as T
        }
        throw RuntimeException("Unknown view model type")
    }
}