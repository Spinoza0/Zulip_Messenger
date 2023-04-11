package org.kimp.tfs.hw7.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.kimp.tfs.hw7.data.api.Profile

class ProfilesRepository(
    private val zulipService: ZulipService
) {
    fun getAuthenticatedUser(): Flow<Profile> = flow {
        emit(zulipService.getAuthenticatedUser())
    }

    fun getAllUsers(): Flow<List<Profile>> = flow {
        emit(zulipService.getAllUsers().members)
    }
}
