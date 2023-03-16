package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.spinoza.messenger_tfs.domain.repository.RepositoryState
import com.spinoza.messenger_tfs.domain.usecase.GetAllChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetRepositoryStateUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetSubscribedChannelsUseCase
import kotlinx.coroutines.flow.StateFlow

class ChannelsFragmentViewModel(
    getRepositoryStateUseCase: GetRepositoryStateUseCase,
    private val getAllChannelsUseCase: GetAllChannelsUseCase,
    private val getSubscribedChannelsUseCase: GetSubscribedChannelsUseCase,
) : ViewModel() {

    val repositoryState: StateFlow<RepositoryState> = getRepositoryStateUseCase()


}