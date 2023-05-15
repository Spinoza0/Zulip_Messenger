package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.User

interface UserRepository {

    suspend fun logIn(email: String, password: String): Result<Boolean>

    suspend fun getOwnUser(): Result<User>

    suspend fun getUser(userId: Long): Result<User>

    suspend fun getAllUsers(): Result<List<User>>

    suspend fun setOwnStatusActive()
}