package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spinoza.messenger_tfs.domain.usecase.GetStateUseCase
import com.spinoza.messenger_tfs.domain.usecase.LoadMessagesUseCase
import com.spinoza.messenger_tfs.domain.usecase.SendMessageUseCase
import com.spinoza.messenger_tfs.domain.usecase.UpdateMessageUseCase

class MessageFragmentViewModelFactory(
    private val getStateUseCase: GetStateUseCase,
    private val loadMessagesUseCase: LoadMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val updateMessageUseCase: UpdateMessageUseCase,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessageFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MessageFragmentViewModel(
                getStateUseCase,
                loadMessagesUseCase,
                sendMessageUseCase,
                updateMessageUseCase,
            ) as T
        }
        throw RuntimeException("Unknown view model type")
    }
}