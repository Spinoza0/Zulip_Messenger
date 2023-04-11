package org.kimp.tfs.hw7.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    @SerialName("user_id")
    val id: Int,
    @SerialName("email")
    val email: String,
    @SerialName("full_name")
    val fullName: String,
    @SerialName("is_admin")
    val isAdmin: Boolean,
    @SerialName("is_owner")
    val isOwner: Boolean,
    @SerialName("is_billing_admin")
    val isBillingAdmin: Boolean,
    @SerialName("role")
    val role: Int,
    @SerialName("is_guest")
    val isGuest: Boolean,
    @SerialName("is_bot")
    val isBot: Boolean,
    @SerialName("is_active")
    val isActive: Boolean,
    @SerialName("timezone")
    val timezone: String,
    @SerialName("date_joined")
    val dateJoined: String,
    @SerialName("avatar_url")
    val avatarUrl: String = "",
    @SerialName("avatar_version")
    val avatarVersion: Int = 0,
    @SerialName("max_message_id")
    val maxMessageId: Int = 0,
    @SerialName("delivery_email")
    val deliveryEmail: String = "",
    @SerialName("profile_data")
    val profileData: HashMap<String, ProfileDataEntry> = hashMapOf()
)
