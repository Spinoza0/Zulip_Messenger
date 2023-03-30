package com.spinoza.messenger_tfs.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDto(
    @SerialName("result") val result: String = "",
    @SerialName("msg") val msg: String = "",
    @SerialName("email") val email: String = "",
    @SerialName("user_id") val userId: Long = UNDEFINED_ID,
    @SerialName("avatar_version") val avatarVersion: Int = 0,
    @SerialName("is_admin") val isAdmin: Boolean = false,
    @SerialName("is_owner") val isOwner: Boolean = false,
    @SerialName("is_guest") val isGuest: Boolean = false,
    @SerialName("is_billing_admin") val isBillingAdmin: Boolean = false,
    @SerialName("role") val role: Int = 0,
    @SerialName("is_bot") val isBot: Boolean = false,
    @SerialName("full_name") val fullName: String = "",
    @SerialName("timezone") val timezone: String = "",
    @SerialName("is_active") val isActive: Boolean = false,
    @SerialName("date_joined") val dateJoined: String = "",
    @SerialName("avatar_url") val avatarUrl: String = "",
    @SerialName("delivery_email") val deliveryEmail: String = "",
    @SerialName("profile_data") val profileData: Map<String, ProfileDataDto> = emptyMap(),
) {
    companion object {
        const val UNDEFINED_ID = -1L
    }
}