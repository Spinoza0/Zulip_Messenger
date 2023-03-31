package com.spinoza.messenger_tfs.data.network

import com.spinoza.messenger_tfs.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ZulipApiService {

    @GET("users/me")
    suspend fun getOwnUser(@Header("Authorization") authHeader: String): Response<OwnResponseDto>

    @GET("users/{$QUERY_USER_ID}")
    suspend fun getUser(
        @Header("Authorization") authHeader: String,
        @Path(QUERY_USER_ID) userId: Long,
    ): Response<UserResponseDto>

    @GET("users")
    suspend fun getAllUsers(
        @Header("Authorization") authHeader: String,
    ): Response<AllUsersResponseDto>

    @GET("users/{$QUERY_USER_ID}/presence")
    suspend fun getUserPresence(
        @Header("Authorization") authHeader: String,
        @Path(QUERY_USER_ID) userId: Long,
    ): Response<PresenceResponseDto>

    @GET("realm/presence")
    suspend fun getAllPresences(
        @Header("Authorization") authHeader: String,
    ): Response<AllPresencesResponseDto>

    @GET("users/me/subscriptions")
    suspend fun getSubscribedStreams(
        @Header("Authorization") authHeader: String,
    ): Response<SubscribedStreamsResponseDto>

    @GET("streams")
    suspend fun getAllStreams(
        @Header("Authorization") authHeader: String,
    ): Response<AllStreamsResponseDto>

    @GET("users/me/{$QUERY_STREAM_ID}/topics")
    suspend fun getTopics(
        @Header("Authorization") authHeader: String,
        @Path(QUERY_STREAM_ID) streamId: Long,
    ): Response<TopicsResponseDto>

    private companion object {
        private const val QUERY_USER_ID = "user_id"
        private const val QUERY_STREAM_ID = "stream_id"
    }
}