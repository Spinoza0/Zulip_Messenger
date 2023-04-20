package org.kimp.tfs.hw7.data

import org.kimp.tfs.hw7.data.api.Profile
import org.kimp.tfs.hw7.data.dto.StreamsResponse
import org.kimp.tfs.hw7.data.dto.SubscribedStreamsResponse
import org.kimp.tfs.hw7.data.dto.TopicsResponse
import org.kimp.tfs.hw7.data.dto.UsersResponse
import org.kimp.tfs.hw7.utils.Authenticated
import retrofit2.http.GET
import retrofit2.http.Path

interface ZulipService {
    @Authenticated
    @GET("/api/v1/users/me")
    suspend fun getAuthenticatedUser(): Profile

    @Authenticated
    @GET("/api/v1/users")
    suspend fun getAllUsers(): UsersResponse

    @Authenticated
    @GET("/api/v1/streams")
    suspend fun getAllStreams(): StreamsResponse

    @Authenticated
    @GET("/api/v1/users/me/subscriptions")
    suspend fun getSubscribedStreams(): SubscribedStreamsResponse

    @Authenticated
    @GET("/api/v1/users/me/{stream}/topics")
    suspend fun getTopicsInStream(@Path("stream") streamId: Int): TopicsResponse
}
