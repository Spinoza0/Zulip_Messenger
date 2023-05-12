package com.spinoza.messenger_tfs.data.network.model

import com.spinoza.messenger_tfs.data.network.apiservice.ZulipResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class WebLimitationsResponse(
    @SerialName("result") override val result: String,
    @SerialName("msg") override val msg: String,
    @SerialName("max_stream_name_length") val maxStreamNameLength: Int,
    @SerialName("max_stream_description_length") val maxStreamDescriptionLength: Int,
    @SerialName("max_topic_length") val maxTopicLength: Int,
    @SerialName("max_message_length") val maxMessageLength: Int,
    @SerialName("server_presence_ping_interval_seconds") val serverPresencePingIntervalSeconds: Int,
    @SerialName("server_presence_offline_threshold_seconds") val serverPresenceOfflineThresholdSeconds: Int,
    @SerialName("realm_message_content_edit_limit_seconds") val messageContentEditLimitSeconds: Int,
    @SerialName("realm_community_topic_editing_limit_seconds") val topicEditingLimitSeconds: Int,
    @SerialName("max_file_upload_size_mib") val maxFileUploadSizeMib: Int,
) : ZulipResponse