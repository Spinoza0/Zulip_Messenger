package com.spinoza.messenger_tfs.domain.model

data class ChannelsFilter(
    val name: String = NO_FILTER,
    val isSubscribed: Boolean = false,
) {

    companion object {

        const val NO_FILTER = ""
    }
}