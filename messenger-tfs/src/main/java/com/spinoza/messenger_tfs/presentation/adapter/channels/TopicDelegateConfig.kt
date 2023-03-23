package com.spinoza.messenger_tfs.presentation.adapter.channels

import com.spinoza.messenger_tfs.domain.model.MessagesFilter

data class TopicDelegateConfig(
    val template: String,
    val evenColor: Int,
    val oddColor: Int,
    val onClickListener: (MessagesFilter) -> Unit,
)