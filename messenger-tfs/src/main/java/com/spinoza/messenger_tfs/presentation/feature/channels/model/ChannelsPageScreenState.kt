package com.spinoza.messenger_tfs.presentation.feature.channels.model

import com.spinoza.messenger_tfs.presentation.adapter.DelegateAdapterItem

data class ChannelsPageScreenState(
    val isLoading: Boolean = false,
    val items: List<DelegateAdapterItem>? = null,
)
