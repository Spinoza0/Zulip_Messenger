package com.spinoza.messenger_tfs.data.network.model

import com.spinoza.messenger_tfs.data.network.apiservice.ZulipResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadFileResponse(
    @SerialName("result") override val result: String,
    @SerialName("msg") override val msg: String,
    @SerialName("uri") val uri: String,
) : ZulipResponse