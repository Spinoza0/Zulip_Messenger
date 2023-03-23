package com.spinoza.messenger_tfs.domain.model

data class ChannelsFilter(
    val narrow: String = NO_FILTER,
) {
    companion object {
        const val NO_FILTER = ""
    }
}