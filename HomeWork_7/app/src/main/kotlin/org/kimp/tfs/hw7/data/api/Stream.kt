package org.kimp.tfs.hw7.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Stream(
    @SerialName("stream_id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
    @SerialName("date_created") val createdDate: Int,
    @SerialName("invite_only") val inviteOnly: Boolean,
    @SerialName("rendered_description") val renderedDescription: String,
    @SerialName("is_web_public") val isWebPublic: Boolean,
    @SerialName("history_public_to_subscribers") val isHistoryPublicFroSubscribers: Boolean,
    @SerialName("is_announcement_only") val isAnnouncementOnly: Boolean,
    @SerialName("can_remove_subscribers_group_id") val canRemoveSubscribersGroupId: Int,
    @SerialName("is_default") val isDefault: Boolean = false,
    @SerialName("message_retention_days") val messageRetentionDays: Int? = null,
)
