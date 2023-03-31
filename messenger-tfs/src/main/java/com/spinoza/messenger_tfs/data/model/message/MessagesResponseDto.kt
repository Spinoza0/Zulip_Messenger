package com.spinoza.messenger_tfs.data.model.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessagesResponseDto(
    @SerialName("result") val result: String,
    @SerialName("msg") val msg: String,
    @SerialName("messages") val messages: List<MessageDto>,
    @SerialName("found_anchor") val foundAnchor: Boolean,
    @SerialName("found_oldest") val foundOldest: Boolean,
    @SerialName("found_newest") val foundNewest: Boolean,
    @SerialName("history_limited") val historyLimited: Boolean,
    @SerialName("anchor") val anchor: Long,
)