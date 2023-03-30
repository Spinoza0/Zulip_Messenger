package com.spinoza.messenger_tfs.data.network

import com.spinoza.messenger_tfs.data.model.UserProfileDto
import retrofit2.http.*

interface ZulipApiService {

    @GET("users/me")
    suspend fun getOwnUser(@Header("Authorization") authHeader: String): UserProfileDto
}