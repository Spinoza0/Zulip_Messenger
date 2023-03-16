package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.spinoza.messenger_tfs.domain.usecase.GetAllChannelsUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetSubscribedChannelsUseCase

class ChannelsFragmentViewModel(
    private val getAllChannelsUseCase: GetAllChannelsUseCase,
    private val getSubscribedChannelsUseCase: GetSubscribedChannelsUseCase,
) : ViewModel() {


}