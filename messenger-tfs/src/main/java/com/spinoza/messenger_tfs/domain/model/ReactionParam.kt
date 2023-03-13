package com.spinoza.messenger_tfs.domain.model

data class ReactionParam(
    val usersIds: List<Int>,
    val isSelected: Boolean,
)