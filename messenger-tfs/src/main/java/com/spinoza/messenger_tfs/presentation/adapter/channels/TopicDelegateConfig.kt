package com.spinoza.messenger_tfs.presentation.adapter.channels

import com.spinoza.messenger_tfs.domain.model.ChannelFilter

data class TopicDelegateConfig(
    val template: String,
    val evenColor: Int,
    val oddColor: Int,
    val onClickListener: (ChannelFilter) -> Unit,
)