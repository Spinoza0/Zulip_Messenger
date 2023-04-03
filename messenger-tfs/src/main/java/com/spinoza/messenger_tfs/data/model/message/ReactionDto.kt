package com.spinoza.messenger_tfs.data.model.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReactionDto(
    @SerialName("emoji_name") val emojiName: String,
    @SerialName("emoji_code") val emojiCode: String,
    @SerialName("reaction_type") val reactionType: String,
    @SerialName("user_id") val userId: Long,
) {

    companion object {

        const val REACTION_TYPE_UNICODE_EMOJI = "unicode_emoji"
        const val REACTION_TYPE_REALM_EMOJI = "realm_emoji"
        const val REACTION_TYPE_ZULIP_EXTRA_EMOJI = "zulip_extra_emoji"
    }
}