package com.spinoza.messenger_tfs.data.network.apiservice

import com.spinoza.messenger_tfs.BuildConfig
import com.spinoza.messenger_tfs.data.network.model.ApiKeyResponse
import com.spinoza.messenger_tfs.data.network.model.BasicResponse
import com.spinoza.messenger_tfs.data.network.model.UploadFileResponse
import com.spinoza.messenger_tfs.data.network.model.event.RegisterEventQueueResponse
import com.spinoza.messenger_tfs.data.network.model.WebLimitationsResponse
import com.spinoza.messenger_tfs.data.network.model.message.MessagesResponse
import com.spinoza.messenger_tfs.data.network.model.message.SendMessageResponse
import com.spinoza.messenger_tfs.data.network.model.message.SingleMessageResponse
import com.spinoza.messenger_tfs.data.network.model.presence.AllPresencesResponse
import com.spinoza.messenger_tfs.data.network.model.presence.PresenceResponse
import com.spinoza.messenger_tfs.data.network.model.stream.AllStreamsResponse
import com.spinoza.messenger_tfs.data.network.model.stream.SubscribedStreamsResponse
import com.spinoza.messenger_tfs.data.network.model.stream.TopicsResponse
import com.spinoza.messenger_tfs.data.network.model.user.AllUsersResponse
import com.spinoza.messenger_tfs.data.network.model.user.OwnUserResponse
import com.spinoza.messenger_tfs.data.network.model.user.UserResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ZulipApiService {

    @POST("fetch_api_key")
    suspend fun fetchApiKey(
        @Query(QUERY_USERNAME) username: String,
        @Query(QUERY_PASSWORD) password: String,
    ): ApiKeyResponse

    @POST("users/me/presence?status=active")
    suspend fun setOwnStatusActive()

    @POST("messages/{$QUERY_MESSAGE_ID}/reactions")
    suspend fun addReaction(
        @Path(QUERY_MESSAGE_ID) messageId: Long,
        @Query(QUERY_EMOJI_NAME) emojiName: String,
    ): BasicResponse

    @POST("messages")
    suspend fun sendMessageToStream(
        @Query(QUERY_TO) streamId: Long,
        @Query(QUERY_TOPIC) topic: String,
        @Query(QUERY_CONTENT) content: String,
        @Query(QUERY_TYPE) type: String = SEND_MESSAGE_TYPE_STREAM,
    ): SendMessageResponse

    @POST("register")
    suspend fun registerEventQueue(
        @Query(QUERY_NARROW) narrow: String = DEFAULT_EMPTY_JSON,
        @Query(QUERY_EVENT_TYPES) eventTypes: String = DEFAULT_EMPTY_JSON,
        @Query(QUERY_APPLY_MARKDOWN) applyMarkdown: Boolean = DEFAULT_APPLY_MARKDOWN,
    ): RegisterEventQueueResponse

    @POST("register")
    suspend fun getWebLimitations(
        @Query(QUERY_FETCH_EVENT_TYPES) fetchEventTypes: String = QUERY_WEB_LIMITATION,
    ): WebLimitationsResponse

    @POST("messages/flags")
    suspend fun setMessageFlagsToRead(
        @Query(QUERY_MESSAGE_IDS) messageIds: String,
        @Query(QUERY_OPERATION) operation: String = QUERY_OPERATION_ADD,
        @Query(QUERY_FLAG) flag: String = QUERY_FLAG_READ,
    ): BasicResponse

    @Multipart
    @POST("user_uploads")
    suspend fun uploadFile(@Part filePart: MultipartBody.Part): UploadFileResponse

    @POST("users/me/subscriptions")
    suspend fun subscribeToStream(@Query(QUERY_SUBSCRIPTIONS) subscriptions: String): BasicResponse

    @GET("users/me")
    suspend fun getOwnUser(): OwnUserResponse

    @GET("users/{$QUERY_USER_ID}")
    suspend fun getUser(@Path(QUERY_USER_ID) userId: Long): UserResponse

    @GET("users")
    suspend fun getAllUsers(): AllUsersResponse

    @GET("users/{$QUERY_USER_ID}/presence")
    suspend fun getUserPresence(@Path(QUERY_USER_ID) userId: Long): PresenceResponse

    @GET("realm/presence")
    suspend fun getAllPresences(): Response<AllPresencesResponse>

    @GET("users/me/subscriptions")
    suspend fun getSubscribedStreams(): SubscribedStreamsResponse

    @GET("streams")
    suspend fun getAllStreams(): AllStreamsResponse

    @GET("users/me/{$QUERY_STREAM_ID}/topics")
    suspend fun getTopics(@Path(QUERY_STREAM_ID) streamId: Long): TopicsResponse

    @GET("messages")
    suspend fun getMessages(
        @Query(QUERY_NUM_BEFORE) numBefore: Int,
        @Query(QUERY_NUM_AFTER) numAfter: Int,
        @Query(QUERY_NARROW) narrow: String,
        @Query(QUERY_ANCHOR) anchor: Long,
        @Query(QUERY_APPLY_MARKDOWN) applyMarkdown: Boolean = DEFAULT_APPLY_MARKDOWN,
    ): MessagesResponse

    @GET("messages")
    suspend fun getMessages(
        @Query(QUERY_NUM_BEFORE) numBefore: Int,
        @Query(QUERY_NUM_AFTER) numAfter: Int,
        @Query(QUERY_NARROW) narrow: String,
        @Query(QUERY_ANCHOR) anchor: String,
        @Query(QUERY_APPLY_MARKDOWN) applyMarkdown: Boolean = DEFAULT_APPLY_MARKDOWN,
    ): MessagesResponse

    @GET("messages/{$QUERY_MESSAGE_ID}")
    suspend fun getSingleMessage(
        @Path(QUERY_MESSAGE_ID) messageId: Long,
        @Query(QUERY_APPLY_MARKDOWN) applyMarkdown: Boolean = DEFAULT_APPLY_MARKDOWN,
    ): SingleMessageResponse

    @GET("events")
    suspend fun getEventsFromQueue(
        @Query(QUERY_QUEUE_ID) queueId: String,
        @Query(QUERY_LAST_EVENT_ID) lastEventId: Long,
    ): Response<ResponseBody>

    @PATCH("messages/{$QUERY_MESSAGE_ID}")
    suspend fun editMessageTopic(
        @Path(QUERY_MESSAGE_ID) messageId: Long,
        @Query(QUERY_TOPIC) topic: String,
        @Query(QUERY_SEND_NOTIFICATION_TO_OLD_THREAD) sendNotificationToOldThread: Boolean = true,
        @Query(QUERY_SEND_NOTIFICATION_TO_NEW_THREAD) sendNotificationToNewThread: Boolean = true,
    ): BasicResponse

    @PATCH("messages/{$QUERY_MESSAGE_ID}")
    suspend fun editMessageContent(
        @Path(QUERY_MESSAGE_ID) messageId: Long,
        @Query(QUERY_CONTENT) content: String,
    ): BasicResponse

    @DELETE("messages/{$QUERY_MESSAGE_ID}/reactions")
    suspend fun removeReaction(
        @Path(QUERY_MESSAGE_ID) messageId: Long,
        @Query(QUERY_EMOJI_NAME) emojiName: String,
    ): BasicResponse

    @DELETE("messages/{$QUERY_MESSAGE_ID}")
    suspend fun deleteMessage(@Path(QUERY_MESSAGE_ID) messageId: Long): BasicResponse

    @DELETE("events")
    suspend fun deleteEventQueue(@Query(QUERY_QUEUE_ID) queueId: String): BasicResponse

    companion object {

        const val RESULT_SUCCESS = "success"
        const val EVENT_HEARTBEAT = "heartbeat"
        const val ANCHOR_NEWEST = "newest"
        const val ANCHOR_OLDEST = "oldest"
        const val ANCHOR_FIRST_UNREAD = "first_unread"
        const val MAX_MESSAGES_PACKET = BuildConfig.MAX_MESSAGES_PACKET
        const val HALF_MESSAGES_PACKET = MAX_MESSAGES_PACKET / 2
        const val EMPTY_MESSAGES_PACKET = 0

        private const val QUERY_SUBSCRIPTIONS = "subscriptions"
        private const val QUERY_USERNAME = "username"
        private const val QUERY_PASSWORD = "password"
        private const val QUERY_USER_ID = "user_id"
        private const val QUERY_NARROW = "narrow"
        private const val QUERY_ANCHOR = "anchor"
        private const val QUERY_NUM_BEFORE = "num_before"
        private const val QUERY_NUM_AFTER = "num_after"
        private const val QUERY_STREAM_ID = "stream_id"
        private const val QUERY_MESSAGE_ID = "message_id"
        private const val QUERY_EMOJI_NAME = "emoji_name"
        private const val QUERY_QUEUE_ID = "queue_id"
        private const val QUERY_LAST_EVENT_ID = "last_event_id"
        private const val QUERY_EVENT_TYPES = "event_types"
        private const val QUERY_APPLY_MARKDOWN = "apply_markdown"
        private const val QUERY_TO = "to"

        private const val QUERY_FETCH_EVENT_TYPES = "fetch_event_types"
        private const val QUERY_WEB_LIMITATION = "[\"realm\"]"

        private const val QUERY_MESSAGE_IDS = "messages"
        private const val QUERY_OPERATION = "op"
        private const val QUERY_OPERATION_ADD = "add"

        private const val QUERY_FLAG = "flag"
        private const val QUERY_FLAG_READ = "read"

        private const val QUERY_TOPIC = "topic"
        private const val QUERY_TYPE = "type"
        private const val QUERY_CONTENT = "content"
        private const val QUERY_SEND_NOTIFICATION_TO_OLD_THREAD = "send_notification_to_old_thread"
        private const val QUERY_SEND_NOTIFICATION_TO_NEW_THREAD = "send_notification_to_new_thread"

        private const val DEFAULT_EMPTY_JSON = "[]"
        private const val DEFAULT_APPLY_MARKDOWN = true

        private const val SEND_MESSAGE_TYPE_STREAM = "stream"
    }
}