package com.spinoza.messenger_tfs.stub

import com.spinoza.messenger_tfs.data.network.model.user.UserDto
import com.spinoza.messenger_tfs.data.utils.toDomain
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.UserRepository

class UserRepositoryStub : UserRepository {

    private val ownUser = createUserDto(0)
    private val user = createUserDto(1)

    override suspend fun logIn(
        email: String,
        password: String,
    ): Result<Boolean> {
        return Result.success(true)
    }

    override suspend fun getOwnUser(): Result<User> {
        return Result.success(ownUser.toDomain(provideUserPresence()))
    }

    override suspend fun getUser(userId: Long): Result<User> {
        return Result.success(user.toDomain(provideUserPresence()))
    }

    override suspend fun getAllUsers(): Result<List<User>> {
        val presence = provideUserPresence()
        val users = listOf(ownUser.toDomain(presence), user.toDomain(presence))
        return Result.success(users)
    }

    override suspend fun setOwnStatusActive() {}

    private fun createUserDto(id: Long): UserDto {
        return UserDto(userId = id)
    }

    private fun provideUserPresence(): User.Presence {
        return User.Presence.ACTIVE
    }
}