package com.spinoza.messenger_tfs.presentation.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.viewmodel.ProfileFragmentViewModel

@Suppress("UNCHECKED_CAST")
class ProfileFragmentViewModelFactory(
    private val getOwnUserUseCase: GetOwnUserUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val registerPresenceEventQueueUseCase: RegisterPresenceEventQueueUseCase,
    private val deletePresenceEventQueueUseCase: DeletePresenceEventQueueUseCase,
    private val getPresenceEventUseCase: GetPresenceEventUseCase,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileFragmentViewModel(
            getOwnUserUseCase,
            getUserUseCase,
            registerPresenceEventQueueUseCase,
            deletePresenceEventQueueUseCase,
            getPresenceEventUseCase
        ) as T
    }
}