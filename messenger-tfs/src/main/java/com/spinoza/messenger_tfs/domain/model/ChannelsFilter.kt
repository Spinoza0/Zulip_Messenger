package com.spinoza.messenger_tfs.domain.model

data class ChannelsFilter(
    val name: String,
    val isSubscribed: Boolean,
) {
    companion object {
        const val NO_FILTER = ""
    }
}