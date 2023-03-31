package com.spinoza.messenger_tfs.data.network

import com.spinoza.messenger_tfs.data.model.*
import com.spinoza.messenger_tfs.data.model.message.MessagesResponseDto
import com.spinoza.messenger_tfs.data.model.message.SendMessageResponseDto
import com.spinoza.messenger_tfs.data.model.message.SingleMessageResponseDto
import com.spinoza.messenger_tfs.data.model.message.UpdateReactionResponseDto
import com.spinoza.messenger_tfs.data.model.presence.AllPresencesResponseDto
import com.spinoza.messenger_tfs.data.model.presence.PresenceResponseDto
import com.spinoza.messenger_tfs.data.model.stream.AllStreamsResponseDto
import com.spinoza.messenger_tfs.data.model.stream.SubscribedStreamsResponseDto
import com.spinoza.messenger_tfs.data.model.stream.TopicsResponseDto
import com.spinoza.messenger_tfs.data.model.user.AllUsersResponseDto
import com.spinoza.messenger_tfs.data.model.user.OwnResponseDto
import com.spinoza.messenger_tfs.data.model.user.UserResponseDto
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

    @GET("messages")
    suspend fun getMessages(
        @Header("Authorization") authHeader: String,
        @Query("num_before") numBefore: Int = DEFAULT_NUM_BEFORE,
        @Query("num_after") numAfter: Int = DEFAULT_NUM_AFTER,
        @Query("anchor") anchor: String = ANCHOR_FIRST_UNREAD,
        @Query("narrow") narrow: String = "",
        @Query("apply_markdown") applyMarkdown: Boolean = false,
        @Query("client_gravatar") clientGravatar: Boolean = false,
    ): Response<MessagesResponseDto>

    @GET("messages/{$QUERY_MESSAGE_ID}")
    suspend fun getSingleMessage(
        @Header("Authorization") authHeader: String,
        @Path(QUERY_MESSAGE_ID) messageId: Long,
    ): Response<SingleMessageResponseDto>

    @POST("messages/{$QUERY_MESSAGE_ID}/reactions")
    suspend fun addReaction(
        @Header("Authorization") authHeader: String,
        @Path(QUERY_MESSAGE_ID) messageId: Long,
        @Path(QUERY_EMOJI_NAME) emojiName: String,
        @Path(QUERY_EMOJI_CODE) emojiCode: String,
    ): Response<UpdateReactionResponseDto>

    @DELETE("messages/{$QUERY_MESSAGE_ID}/reactions")
    suspend fun removeReaction(
        @Header("Authorization") authHeader: String,
        @Path(QUERY_MESSAGE_ID) messageId: Long,
        @Path(QUERY_EMOJI_NAME) emojiName: String,
        @Path(QUERY_EMOJI_CODE) emojiCode: String,
    ): Response<UpdateReactionResponseDto>

    @POST("messages")
    suspend fun sendMessageToStream(
        @Header("Authorization") authHeader: String,
        @Query("to") streamId: Long,
        @Query("topic") topic: String,
        @Query("content") content: String,
        @Query("type") type: String = SEND_MESSAGE_TYPE_STREAM,
    ): Response<SendMessageResponseDto>

    companion object {

        const val ANCHOR_NEWEST = "newest"
        const val ANCHOR_OLDEST = "oldest"
        const val ANCHOR_FIRST_UNREAD = "first_unread"

        private const val QUERY_USER_ID = "user_id"
        private const val QUERY_STREAM_ID = "stream_id"
        private const val QUERY_MESSAGE_ID = "message_id"
        private const val QUERY_EMOJI_NAME = "emoji_name"
        private const val QUERY_EMOJI_CODE = "emoji_code"

        private const val DEFAULT_NUM_BEFORE = 50
        private const val DEFAULT_NUM_AFTER = 50

        private const val SEND_MESSAGE_TYPE_PRIVATE = "private"
        private const val SEND_MESSAGE_TYPE_STREAM = "stream"
    }
}