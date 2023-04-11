package org.kimp.tfs.hw7.domain

import kotlinx.coroutines.flow.Flow
import org.kimp.tfs.hw7.data.ProfilesRepository
import org.kimp.tfs.hw7.data.api.Profile

interface ProfilesInteractor {
    fun getAuthenticatedUser(): Flow<Profile>
    fun getAllUsers(): Flow<List<Profile>>
}


class ProfilesInteractorImpl(
    private val profilesRepository: ProfilesRepository
) : ProfilesInteractor {
    override fun getAuthenticatedUser(): Flow<Profile> = profilesRepository.getAuthenticatedUser()
    override fun getAllUsers(): Flow<List<Profile>> = profilesRepository.getAllUsers()
}

