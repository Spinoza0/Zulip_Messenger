package com.spinoza.messenger_tfs.presentation.adapter.topic

import com.spinoza.messenger_tfs.domain.model.Channel

data class TopicAdapterConfig(
    val template: String,
    val evenColor: Int,
    val oddColor: Int,
    val onClickListener: (Channel, String) -> Unit,
)