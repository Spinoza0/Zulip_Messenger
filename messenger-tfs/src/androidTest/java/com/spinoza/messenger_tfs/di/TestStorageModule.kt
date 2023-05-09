package com.spinoza.messenger_tfs.di

import com.spinoza.messenger_tfs.BuildConfig
import com.spinoza.messenger_tfs.data.database.MessengerDao
import com.spinoza.messenger_tfs.data.database.model.MessageDataDbModel
import com.spinoza.messenger_tfs.data.database.model.MessageDbModel
import com.spinoza.messenger_tfs.data.database.model.ReactionDbModel
import com.spinoza.messenger_tfs.data.database.model.StreamDbModel
import com.spinoza.messenger_tfs.data.database.model.TopicDbModel
import com.spinoza.messenger_tfs.domain.network.AuthorizationStorage
import dagger.Module
import dagger.Provides

@Module
object TestStorageModule {

    @ApplicationScope
    @Provides
    fun provideAuthorizationStorage(): AuthorizationStorage = object : AuthorizationStorage {

        override fun isUserLoggedIn(): Boolean = true

        override fun isAdmin(): Boolean = false

        override fun isAuthorizationDataExisted(): Boolean = true

        override fun makeAuthHeader(email: String, apiKey: String): String = "AuthHeader"

        override fun getAuthHeaderTitle(): String = "Title"

        override fun getAuthHeaderValue(): String = "Value"

        override fun getUserId(): Long = 604180

        override fun saveData(
            userId: Long,
            isAdmin: Boolean,
            email: String,
            password: String,
            apiKey: String,
        ) {
        }

        override fun getEmail(): String = "email"

        override fun getPassword(): String = "password"

        override fun deleteData() {}
    }

    @ApplicationScope
    @Provides
    @BaseUrl
    fun provideBaseUrl(): String = "http://localhost:${BuildConfig.MOCKWEBSERVER_PORT}"

    @ApplicationScope
    @Provides
    fun provideMessengerDao(): MessengerDao = object : MessengerDao {

        override suspend fun getStreams(): List<StreamDbModel> = emptyList()

        override suspend fun getTopics(streamId: Long, isSubscribed: Boolean): List<TopicDbModel> =
            emptyList()

        override suspend fun insertTopics(topics: List<TopicDbModel>) {}

        override suspend fun insertStreams(streams: List<StreamDbModel>) {}

        override suspend fun removeTopics(streamId: Long, isSubscribed: Boolean) {}

        override suspend fun removeStreams(isSubscribed: Boolean) {}

        override suspend fun getMessages(): List<MessageDbModel> = emptyList()

        override suspend fun removeMessages() {}

        override suspend fun insertMessage(message: MessageDataDbModel) {}

        override suspend fun insertReaction(reaction: ReactionDbModel) {}
    }
}