package com.spinoza.messenger_tfs.presentation.model.messages

import com.spinoza.messenger_tfs.R

data class MessagesState(
    val isLoading: Boolean = false,
    val isSendingMessage: Boolean = false,
    val messages: MessagesResultDelegate? = null,
    val iconActionResId: Int = R.drawable.ic_add_circle_outline,
)