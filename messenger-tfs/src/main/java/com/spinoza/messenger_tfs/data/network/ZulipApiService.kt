package com.spinoza.messenger_tfs.data.network

import com.spinoza.messenger_tfs.data.model.*
import com.spinoza.messenger_tfs.data.model.event.RegisterEventQueueResponse
import com.spinoza.messenger_tfs.data.model.message.MessagesResponse
import com.spinoza.messenger_tfs.data.model.message.SendMessageResponse
import com.spinoza.messenger_tfs.data.model.message.SingleMessageResponse
import com.spinoza.messenger_tfs.data.model.presence.AllPresencesResponse
import com.spinoza.messenger_tfs.data.model.presence.PresenceResponse
import com.spinoza.messenger_tfs.data.model.stream.AllStreamsResponse
import com.spinoza.messenger_tfs.data.model.stream.SubscribedStreamsResponse
import com.spinoza.messenger_tfs.data.model.stream.TopicsResponse
import com.spinoza.messenger_tfs.data.model.user.AllUsersResponse
import com.spinoza.messenger_tfs.data.model.user.OwnUserResponse
import com.spinoza.messenger_tfs.data.model.user.UserResponse
import retrofit2.Response
import retrofit2.http.*

interface ZulipApiService {

    @GET("users/me")
    suspend fun getOwnUser(
        @Header(HEADER_AUTHORIZATION) authHeader: String,
    ): Response<OwnUserResponse>

    @GET("users/{$QUERY_USER_ID}")
    suspend fun getUser(
        @Header(HEADER_AUTHORIZATION) authHeader: String,
        @Path(QUERY_USER_ID) userId: Long,
    ): Response<UserResponse>

    @GET("users")
    suspend fun getAllUsers(
        @Header(HEADER_AUTHORIZATION) authHeader: String,
    ): Response<AllUsersResponse>

    @GET("users/{$QUERY_USER_ID}/presence")
    suspend fun getUserPresence(
        @Header(HEADER_AUTHORIZATION) authHeader: String,
        @Path(QUERY_USER_ID) userId: Long,
    ): Response<PresenceResponse>

    @GET("realm/presence")
    suspend fun getAllPresences(
        @Header(HEADER_AUTHORIZATION) authHeader: String,
    ): Response<AllPresencesResponse>

    @GET("users/me/subscriptions")
    suspend fun getSubscribedStreams(
        @Header(HEADER_AUTHORIZATION) authHeader: String,
    ): Response<SubscribedStreamsResponse>

    @GET("streams")
    suspend fun getAllStreams(
        @Header(HEADER_AUTHORIZATION) authHeader: String,
    ): Response<AllStreamsResponse>

    @GET("users/me/{$QUERY_STREAM_ID}/topics")
    suspend fun getTopics(
        @Header(HEADER_AUTHORIZATION) authHeader: String,
        @Path(QUERY_STREAM_ID) streamId: Long,
    ): Response<TopicsResponse>

    @GET("messages")
    suspend fun getMessages(
        @Header(HEADER_AUTHORIZATION) authHeader: String,
        @Query(QUERY_NUM_BEFORE) numBefore: Int = DEFAULT_NUM_BEFORE,
        @Query(QUERY_NUM_AFTER) numAfter: Int = DEFAULT_NUM_AFTER,
        @Query(QUERY_ANCHOR) anchor: String = ANCHOR_FIRST_UNREAD,
        @Query(QUERY_NARROW) narrow: String = "",
        @Query(QUERY_APPLY_MARKDOWN) applyMarkdown: Boolean = false,
    ): Response<MessagesResponse>

    @GET("messages/{$QUERY_MESSAGE_ID}")
    suspend fun getSingleMessage(
        @Header(HEADER_AUTHORIZATION) authHeader: String,
        @Path(QUERY_MESSAGE_ID) messageId: Long,
    ): Response<SingleMessageResponse>

    @POST("messages/{$QUERY_MESSAGE_ID}/reactions")
    suspend fun addReaction(
        @Header(HEADER_AUTHORIZATION) authHeader: String,
        @Path(QUERY_MESSAGE_ID) messageId: Long,
        @Query(QUERY_EMOJI_NAME) emojiName: String,
    ): Response<BasicResponse>

    @DELETE("messages/{$QUERY_MESSAGE_ID}/reactions")
    suspend fun removeReaction(
        @Header(HEADER_AUTHORIZATION) authHeader: String,
        @Path(QUERY_MESSAGE_ID) messageId: Long,
        @Query(QUERY_EMOJI_NAME) emojiName: String,
    ): Response<BasicResponse>

    @POST("messages")
    suspend fun sendMessageToStream(
        @Header(HEADER_AUTHORIZATION) authHeader: String,
        @Query(QUERY_TO) streamId: Long,
        @Query(QUERY_TOPIC) topic: String,
        @Query(QUERY_CONTENT) content: String,
        @Query(QUERY_TYPE) type: String = SEND_MESSAGE_TYPE_STREAM,
    ): Response<SendMessageResponse>

    @POST("register")
    suspend fun registerEventQueue(
        @Header(HEADER_AUTHORIZATION) authHeader: String,
        @Query(QUERY_NARROW) narrow: String = "",
        @Query(QUERY_EVENT_TYPES) evenTypes: String = "",
        @Query(QUERY_APPLY_MARKDOWN) applyMarkdown: Boolean = false,
    ): Response<RegisterEventQueueResponse>

    @POST("events")
    suspend fun deleteEventQueue(
        @Header(HEADER_AUTHORIZATION) authHeader: String,
        @Query(QUERY_QUEUE_ID) queueId: String,
    ): Response<BasicResponse>

    companion object {

        const val ANCHOR_NEWEST = "newest"
        const val ANCHOR_OLDEST = "oldest"
        const val ANCHOR_FIRST_UNREAD = "first_unread"

        private const val HEADER_AUTHORIZATION = "Authorization"

        private const val QUERY_USER_ID = "user_id"
        private const val QUERY_NARROW = "narrow"
        private const val QUERY_ANCHOR = "anchor"
        private const val QUERY_NUM_BEFORE = "num_before"
        private const val QUERY_NUM_AFTER = "num_after"
        private const val QUERY_STREAM_ID = "stream_id"
        private const val QUERY_MESSAGE_ID = "message_id"
        private const val QUERY_EMOJI_NAME = "emoji_name"
        private const val QUERY_QUEUE_ID = "queue_id"
        private const val QUERY_EVENT_TYPES = "event_types"
        private const val QUERY_APPLY_MARKDOWN = "apply_markdown"
        private const val QUERY_TO = "to"
        private const val QUERY_TOPIC = "topic"
        private const val QUERY_TYPE = "type"
        private const val QUERY_CONTENT = "content"

        private const val DEFAULT_NUM_BEFORE = 50
        private const val DEFAULT_NUM_AFTER = 50

        private const val SEND_MESSAGE_TYPE_PRIVATE = "private"
        private const val SEND_MESSAGE_TYPE_STREAM = "stream"
    }
}