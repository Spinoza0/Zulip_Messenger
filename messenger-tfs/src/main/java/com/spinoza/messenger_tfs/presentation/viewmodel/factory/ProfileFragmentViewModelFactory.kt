package com.spinoza.messenger_tfs.presentation.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.viewmodel.ProfileFragmentViewModel

@Suppress("UNCHECKED_CAST")
class ProfileFragmentViewModelFactory(
    private val getOwnUserUseCase: GetOwnUserUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val registerEventQueueUseCase: RegisterEventQueueUseCase,
    private val deletePresenceEventQueueUseCase: DeletePresenceEventQueueUseCase,
    private val getPresenceEventsUseCase: GetPresenceEventsUseCase,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileFragmentViewModel(
            getOwnUserUseCase,
            getUserUseCase,
            registerEventQueueUseCase,
            deletePresenceEventQueueUseCase,
            getPresenceEventsUseCase
        ) as T
    }
}