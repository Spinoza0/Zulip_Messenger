package com.spinoza.messenger_tfs.domain.model

data class Reaction(
    val emoji: String,
    val count: Int,
    val isSelected: Boolean,
    val isCountVisible: Boolean = true,
    val isBackgroundVisible: Boolean = true,
)