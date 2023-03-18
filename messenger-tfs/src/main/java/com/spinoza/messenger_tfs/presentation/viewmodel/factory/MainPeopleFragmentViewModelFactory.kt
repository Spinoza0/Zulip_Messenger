package com.spinoza.messenger_tfs.presentation.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spinoza.messenger_tfs.domain.usecase.*
import com.spinoza.messenger_tfs.presentation.viewmodel.MainPeopleFragmentViewModel

@Suppress("UNCHECKED_CAST")
class MainPeopleFragmentViewModelFactory(
    private val getAllUsersUseCase: GetAllUsersUseCase,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainPeopleFragmentViewModel(getAllUsersUseCase) as T
    }
}