package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.network.apiservice.ZulipApiService
import com.spinoza.messenger_tfs.data.network.apiservice.ZulipApiService.Companion.RESULT_SUCCESS
import com.spinoza.messenger_tfs.data.network.model.ApiKeyResponse
import com.spinoza.messenger_tfs.data.network.model.WebLimitationsResponse
import com.spinoza.messenger_tfs.data.network.model.presence.AllPresencesResponse
import com.spinoza.messenger_tfs.data.network.model.user.AllUsersResponse
import com.spinoza.messenger_tfs.data.network.model.user.OwnUserResponse
import com.spinoza.messenger_tfs.data.network.model.user.UserResponse
import com.spinoza.messenger_tfs.data.utils.apiRequest
import com.spinoza.messenger_tfs.data.utils.runCatchingNonCancellation
import com.spinoza.messenger_tfs.data.utils.toDomain
import com.spinoza.messenger_tfs.di.DispatcherIO
import com.spinoza.messenger_tfs.domain.model.RepositoryError
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.network.AuthorizationStorage
import com.spinoza.messenger_tfs.domain.network.WebLimitation
import com.spinoza.messenger_tfs.domain.repository.UserRepository
import com.spinoza.messenger_tfs.domain.util.EMPTY_STRING
import com.spinoza.messenger_tfs.domain.util.getCurrentTimestamp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ZulipApiService,
    private val authorizationStorage: AuthorizationStorage,
    private val webLimitation: WebLimitation,
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) : UserRepository {

    override suspend fun logIn(email: String, password: String): Result<Boolean> =
        withContext(ioDispatcher) {
            authorizationStorage.makeAuthHeader(email)
            if (saveOwnUserData(email, password)) {
                updateWebLimitations()
                return@withContext Result.success(true)
            }
            runCatchingNonCancellation {
                val apiKeyResponse =
                    apiRequest<ApiKeyResponse> { apiService.fetchApiKey(email, password) }
                authorizationStorage.makeAuthHeader(apiKeyResponse.email, apiKeyResponse.apiKey)
                updateWebLimitations()
                saveOwnUserData(email, password, apiKeyResponse.apiKey)
            }
        }

    private suspend fun updateWebLimitations() {
        runCatchingNonCancellation {
            val response = apiRequest<WebLimitationsResponse> { apiService.getWebLimitations() }
            webLimitation.updateLimitations(
                response.maxStreamNameLength,
                response.maxStreamDescriptionLength,
                response.maxTopicLength,
                response.maxMessageLength,
                response.serverPresencePingIntervalSeconds,
                response.serverPresenceOfflineThresholdSeconds,
                response.messageContentEditLimitSeconds,
                response.topicEditingLimitSeconds,
                response.maxFileUploadSizeMib
            )
        }
    }

    private suspend fun saveOwnUserData(
        email: String,
        password: String,
        apiKey: String = EMPTY_STRING,
    ): Boolean {
        if (authorizationStorage.getAuthHeaderValue().isBlank()) return false
        runCatchingNonCancellation {
            apiRequest<OwnUserResponse> { apiService.getOwnUser() }
        }.onSuccess {
            authorizationStorage.saveData(it.userId, it.isAdmin, email, password, apiKey)
            return true
        }
        return false
    }

    override suspend fun getOwnUser(): Result<User> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val ownUserResponse = apiRequest<OwnUserResponse> { apiService.getOwnUser() }
            val presence = getUserPresence(ownUserResponse.userId)
            ownUserResponse.toDomain(presence)
        }
    }

    override suspend fun getUser(userId: Long): Result<User> = withContext(ioDispatcher) {
        runCatchingNonCancellation {
            val userResponse = apiRequest<UserResponse> { apiService.getUser(userId) }
            val presence = getUserPresence(userResponse.user.userId)
            userResponse.user.toDomain(presence)
        }
    }

    override suspend fun getAllUsers(): Result<List<User>> =
        withContext(ioDispatcher) {
            runCatchingNonCancellation {
                val allUsersResponse = apiRequest<AllUsersResponse> { apiService.getAllUsers() }
                val presencesResponse = apiService.getAllPresences()
                if (presencesResponse.isSuccessful) {
                    makeAllUsersAnswer(allUsersResponse, presencesResponse.body())
                } else {
                    makeAllUsersAnswer(allUsersResponse)
                }
            }
        }

    override suspend fun setOwnStatusActive() {
        withContext(ioDispatcher) {
            runCatchingNonCancellation { apiService.setOwnStatusActive() }
        }
    }

    private suspend fun getUserPresence(userId: Long): User.Presence = runCatchingNonCancellation {
        val presenceResponse = apiService.getUserPresence(userId)
        if (presenceResponse.result == RESULT_SUCCESS) {
            presenceResponse.presence.toDomain()
        } else {
            User.Presence.OFFLINE
        }
    }.getOrElse {
        User.Presence.OFFLINE
    }

    private fun makeAllUsersAnswer(
        usersResponse: AllUsersResponse,
        presencesResponse: AllPresencesResponse? = null,
    ): List<User> {
        if (usersResponse.result != RESULT_SUCCESS) {
            throw RepositoryError(usersResponse.msg)
        }
        val users = mutableListOf<User>()
        val timestamp = getCurrentTimestamp()
        usersResponse.members
            .filter { it.isBot.not() && it.isActive }
            .forEach { userDto ->
                val presence =
                    if (presencesResponse != null && presencesResponse.result == RESULT_SUCCESS) {
                        presencesResponse.presences[userDto.email]?.let { presenceDto ->
                            if ((timestamp - presenceDto.aggregated.timestamp) <
                                webLimitation.getPresenceOfflineThresholdSeconds()
                            ) {
                                presenceDto.toDomain()
                            } else {
                                User.Presence.OFFLINE
                            }
                        } ?: User.Presence.OFFLINE
                    } else {
                        User.Presence.OFFLINE
                    }
                users.add(userDto.toDomain(presence))
            }
        return users
    }
}