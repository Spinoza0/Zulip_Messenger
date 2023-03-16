package com.spinoza.messenger_tfs.presentation.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spinoza.messenger_tfs.domain.usecase.GetRepositoryStateUseCase
import com.spinoza.messenger_tfs.domain.usecase.SendMessageUseCase
import com.spinoza.messenger_tfs.domain.usecase.UpdateReactionUseCase
import com.spinoza.messenger_tfs.presentation.viewmodel.MessagesViewModel

@Suppress("UNCHECKED_CAST")
class MessageViewModelFactory(
    private val getRepositoryStateUseCase: GetRepositoryStateUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val updateReactionUseCase: UpdateReactionUseCase,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MessagesViewModel(
            getRepositoryStateUseCase,
            sendMessageUseCase,
            updateReactionUseCase
        ) as T
    }
}