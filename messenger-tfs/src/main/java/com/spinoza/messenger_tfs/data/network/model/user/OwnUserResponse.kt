package com.spinoza.messenger_tfs.data.network.model.user

import com.spinoza.messenger_tfs.data.network.apiservice.ZulipResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OwnUserResponse(
    @SerialName("result") override val result: String,
    @SerialName("msg") override val msg: String,
    @SerialName("email") val email: String,
    @SerialName("user_id") val userId: Long,
    @SerialName("avatar_version") val avatarVersion: Int,
    @SerialName("is_admin") val isAdmin: Boolean,
    @SerialName("is_owner") val isOwner: Boolean,
    @SerialName("is_guest") val isGuest: Boolean,
    @SerialName("is_billing_admin") val isBillingAdmin: Boolean,
    @SerialName("role") val role: Int,
    @SerialName("is_bot") val isBot: Boolean,
    @SerialName("full_name") val fullName: String,
    @SerialName("timezone") val timezone: String,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("date_joined") val dateJoined: String,
    @SerialName("avatar_url") val avatarUrl: String?,
    @SerialName("delivery_email") val deliveryEmail: String?,
    @SerialName("profile_data") val profileData: Map<String, ProfileDataDto>,
) : ZulipResponse