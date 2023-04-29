package com.spinoza.messenger_tfs.presentation.feature.messages.model

import com.spinoza.messenger_tfs.R

data class MessagesScreenState(
    val isLoading: Boolean = false,
    val isLongOperation: Boolean = false,
    val isSendingMessage: Boolean = false,
    val isNextMessageExisting: Boolean = false,
    val isNewMessageExisting: Boolean = false,
    val messages: MessagesResultDelegate? = null,
    val iconActionResId: Int = R.drawable.ic_add_circle_outline,
)