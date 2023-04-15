package com.spinoza.messenger_tfs.presentation.model.channels

import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem

data class ChannelsPageScreenState(
    val isLoading: Boolean = false,
    val items: List<DelegateAdapterItem>? = null,
)
