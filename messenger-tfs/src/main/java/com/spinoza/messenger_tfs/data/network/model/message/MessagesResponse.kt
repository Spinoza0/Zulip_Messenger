package com.spinoza.messenger_tfs.data.network.model.message

import com.spinoza.messenger_tfs.data.network.apiservice.ZulipResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessagesResponse(
    @SerialName("result") override val result: String,
    @SerialName("msg") override val msg: String,
    @SerialName("messages") val messages: List<MessageDto>,
    @SerialName("found_anchor") val foundAnchor: Boolean,
    @SerialName("found_oldest") val foundOldest: Boolean,
    @SerialName("found_newest") val foundNewest: Boolean,
    @SerialName("history_limited") val historyLimited: Boolean,
    @SerialName("anchor") val anchor: Long,
) : ZulipResponse