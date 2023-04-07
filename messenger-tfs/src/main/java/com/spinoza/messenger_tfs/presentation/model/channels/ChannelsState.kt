package com.spinoza.messenger_tfs.presentation.model.channels

import com.spinoza.messenger_tfs.presentation.model.SearchQuery

data class ChannelsState(
    val filter: SearchQuery? = null,
)
