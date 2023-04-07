package com.spinoza.messenger_tfs.presentation.model.channelspage

import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem

data class ChannelsPageState(
    val isLoading: Boolean = false,
    val items: List<DelegateAdapterItem>? = null,
)
