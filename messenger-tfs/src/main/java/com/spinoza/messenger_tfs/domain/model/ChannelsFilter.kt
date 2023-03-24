package com.spinoza.messenger_tfs.domain.model

data class ChannelsFilter(
    val value: String = NO_FILTER,
) {
    companion object {
        const val NO_FILTER = ""
    }
}