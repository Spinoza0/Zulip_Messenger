package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.spinoza.messenger_tfs.domain.usecase.GetCurrentUserUseCase

class MainProfileFragmentViewModel(
    getCurrentUserUseCase: GetCurrentUserUseCase,
) : ViewModel() {

    val user = getCurrentUserUseCase()
}