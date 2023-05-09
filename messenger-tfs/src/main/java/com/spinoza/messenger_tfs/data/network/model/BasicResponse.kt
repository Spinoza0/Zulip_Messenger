package com.spinoza.messenger_tfs.data.network.model

import com.spinoza.messenger_tfs.data.network.apiservice.ZulipResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BasicResponse(
    @SerialName("result") override val result: String,
    @SerialName("msg") override val msg: String,
) : ZulipResponse