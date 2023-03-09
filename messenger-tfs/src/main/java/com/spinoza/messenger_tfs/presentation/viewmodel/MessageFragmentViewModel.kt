package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.domain.model.RepositoryState
import com.spinoza.messenger_tfs.domain.usecase.GetStateUseCase
import com.spinoza.messenger_tfs.domain.usecase.LoadMessagesUseCase
import kotlinx.coroutines.launch

class MessageFragmentViewModel(
    private val getStateUseCase: GetStateUseCase,
    private val loadMessagesUseCase: LoadMessagesUseCase,
) : ViewModel() {

    init {
        loadMessages()
    }

    fun getState(): LiveData<RepositoryState> {
        return getStateUseCase()
    }

    fun loadMessages() {
        viewModelScope.launch {
            loadMessagesUseCase()
        }
    }

}