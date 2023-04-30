package com.spinoza.messenger_tfs.stub

import com.spinoza.messenger_tfs.data.network.ZulipApiService
import com.spinoza.messenger_tfs.data.network.model.ApiKeyResponse
import com.spinoza.messenger_tfs.data.network.model.BasicResponse
import com.spinoza.messenger_tfs.data.network.model.UploadFileResponse
import com.spinoza.messenger_tfs.data.network.model.event.RegisterEventQueueResponse
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

class ApiServiceStub: ZulipApiService {

    override suspend fun fetchApiKey(username: String, password: String): Response<ApiKeyResponse> {
        throw RuntimeException("Not yet implemented")
    }

    override suspend fun getOwnUser(): Response<OwnUserResponse> {
        throw RuntimeException("Not yet implemented")
    }

    override suspend fun getUser(userId: Long): Response<UserResponse> {
        throw RuntimeException("Not yet implemented")
    }

    override suspend fun getAllUsers(): Response<AllUsersResponse> {
        throw RuntimeException("Not yet implemented")
    }

    override suspend fun getUserPresence(userId: Long): Response<PresenceResponse> {
        throw RuntimeException("Not yet implemented")
    }

    override suspend fun getAllPresences(): Response<AllPresencesResponse> {
        throw RuntimeException("Not yet implemented")
    }

    override suspend fun setOwnStatusActive() {
        throw RuntimeException("Not yet implemented")
    }

    override suspend fun getSubscribedStreams(): Response<SubscribedStreamsResponse> {
        throw RuntimeException("Not yet implemented")
    }

    override suspend fun getAllStreams(): Response<AllStreamsResponse> {
        throw RuntimeException("Not yet implemented")
    }

    override suspend fun getTopics(streamId: Long): Response<TopicsResponse> {
        throw RuntimeException("Not yet implemented")
    }

    override suspend fun getMessages(
        numBefore: Int,
        numAfter: Int,
        narrow: String,
        anchor: Long,
        applyMarkdown: Boolean,
    ): Response<MessagesResponse> {
        throw RuntimeException("Not yet implemented")
    }

    override suspend fun getMessages(
        numBefore: Int,
        numAfter: Int,
        narrow: String,
        anchor: String,
        applyMarkdown: Boolean,
    ): Response<MessagesResponse> {
        throw RuntimeException("Not yet implemented")
    }

    override suspend fun getSingleMessage(messageId: Long): Response<SingleMessageResponse> {
        throw RuntimeException("Not yet implemented")
    }

    override suspend fun addReaction(messageId: Long, emojiName: String): Response<BasicResponse> {
        throw RuntimeException("Not yet implemented")
    }

    override suspend fun removeReaction(
        messageId: Long,
        emojiName: String,
    ): Response<BasicResponse> {
        throw RuntimeException("Not yet implemented")
    }

    override suspend fun sendMessageToStream(
        streamId: Long,
        topic: String,
        content: String,
        type: String,
    ): Response<SendMessageResponse> {
        throw RuntimeException("Not yet implemented")
    }

    override suspend fun registerEventQueue(
        narrow: String,
        eventTypes: String,
        applyMarkdown: Boolean,
    ): Response<RegisterEventQueueResponse> {
        throw RuntimeException("Not yet implemented")
    }

    override suspend fun deleteEventQueue(queueId: String): Response<BasicResponse> {
        throw RuntimeException("Not yet implemented")
    }

    override suspend fun getEventsFromQueue(
        queueId: String,
        lastEventId: Long,
    ): Response<ResponseBody> {
        throw RuntimeException("Not yet implemented")
    }

    override suspend fun setMessageFlagsToRead(
        messageIds: String,
        operation: String,
        flag: String,
    ): Response<BasicResponse> {
        throw RuntimeException("Not yet implemented")
    }

    override suspend fun uploadFile(filePart: MultipartBody.Part): Response<UploadFileResponse> {
        throw RuntimeException("Not yet implemented")
    }
}