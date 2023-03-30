package com.spinoza.messenger_tfs.data.network

import com.spinoza.messenger_tfs.data.model.PresenceResponseDto
import com.spinoza.messenger_tfs.data.model.UserResponseDto
import retrofit2.Response
import retrofit2.http.*

interface ZulipApiService {

    @GET("users/me")
    suspend fun getOwnUser(@Header("Authorization") authHeader: String): Response<UserResponseDto>

    @GET("users/{$QUERY_USER_ID}")
    suspend fun getUser(
        @Header("Authorization") authHeader: String,
        @Path(QUERY_USER_ID) userId: Long,
    ): Response<UserResponseDto>

    @GET("users/{$QUERY_USER_ID}/presence")
    suspend fun getUserPresence(
        @Header("Authorization") authHeader: String,
        @Path(QUERY_USER_ID) userId: Long,
    ): PresenceResponseDto

    private companion object {
        private const val QUERY_USER_ID = "user_id"
    }
}