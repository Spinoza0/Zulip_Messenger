package com.spinoza.messenger_tfs.presentation.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.viewmodel.MessagesFragmentViewModel

@Suppress("UNCHECKED_CAST")
class MessagesFragmentViewModelFactory(
    private val getRepositoryStateUseCase: GetRepositoryStateUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val updateReactionUseCase: UpdateReactionUseCase,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MessagesFragmentViewModel(
            getRepositoryStateUseCase,
            sendMessageUseCase,
            updateReactionUseCase
        ) as T
    }
}