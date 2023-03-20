package com.spinoza.messenger_tfs.presentation.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spinoza.messenger_tfs.domain.model.ChannelFilter
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.viewmodel.MessagesFragmentViewModel

@Suppress("UNCHECKED_CAST")
class MessagesFragmentViewModelFactory(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val updateReactionUseCase: UpdateReactionUseCase,
    private val channelFilter: ChannelFilter,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MessagesFragmentViewModel(
            getCurrentUserUseCase,
            getMessagesUseCase,
            sendMessageUseCase,
            updateReactionUseCase,
            channelFilter,
        ) as T
    }
}