package com.spinoza.messenger_tfs.presentation.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.viewmodel.PeopleFragmentViewModel

@Suppress("UNCHECKED_CAST")
class PeopleFragmentViewModelFactory(
    private val getUsersByFilterUseCase: GetUsersByFilterUseCase,
    private val registerEventQueueUseCase: RegisterEventQueueUseCase,
    private val deleteEventQueueUseCase: DeleteEventQueueUseCase,
    private val getPresenceEventsUseCase: GetPresenceEventsUseCase,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PeopleFragmentViewModel(
            getUsersByFilterUseCase,
            registerEventQueueUseCase,
            deleteEventQueueUseCase,
            getPresenceEventsUseCase
        ) as T
    }
}