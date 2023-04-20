package org.kimp.tfs.hw7.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.kimp.tfs.hw7.data.api.Profile

@Serializable
data class UsersResponse(
    @SerialName("members") val members: List<Profile>
)
