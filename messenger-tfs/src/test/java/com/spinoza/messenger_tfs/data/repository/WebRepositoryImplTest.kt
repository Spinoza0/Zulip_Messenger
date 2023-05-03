package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.cache.MessagesCache
import com.spinoza.messenger_tfs.data.database.MessengerDaoProviderImpl
import com.spinoza.messenger_tfs.data.network.AppAuthKeeperImpl
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.repository.WebRepository
import com.spinoza.messenger_tfs.stub.ApiServiceProviderStub
import com.spinoza.messenger_tfs.stub.MessagesGenerator
import com.spinoza.messenger_tfs.stub.MessengerDaoStub
import com.spinoza.messenger_tfs.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WebRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val messagesGenerator = MessagesGenerator()

    @Before
    fun setUp() {
        messagesGenerator.reset()
    }

    @Test
    fun `getStoredMessages returns empty list of messages`() = runTest {
        val repository = createRepository(MessengerDaoStub.Type.EMPTY)
        val messagesFilter = provideMessagesFilter(messagesGenerator)

        val result = repository.getStoredMessages(messagesFilter)

        assertEquals(true, result.getOrNull()?.messages?.isEmpty())
    }

    @Test
    fun `getStoredMessages returns not empty list of messages`() = runTest {
        val repository = createRepository(MessengerDaoStub.Type.WITH_MESSAGES)
        val messagesFilter = provideMessagesFilter(messagesGenerator)

        val result = repository.getStoredMessages(messagesFilter)

        assertEquals(true, result.getOrNull()?.messages?.isNotEmpty())
    }

    @Test
    fun `getStoredMessages returns error`() = runTest {
        val repository = createRepository(MessengerDaoStub.Type.WITH_GET_MESSAGES_ERROR)
        val messagesFilter = provideMessagesFilter(messagesGenerator)

        val result = repository.getStoredMessages(messagesFilter)

        assertEquals(true, result.isFailure)
    }

    private fun provideMessagesFilter(messagesGenerator: MessagesGenerator): MessagesFilter {
        val streamId = messagesGenerator.getStreamId()
        val topicName = messagesGenerator.getTopicName()
        return MessagesFilter(
            channel = Channel(channelId = streamId),
            topic = Topic(name = topicName, channelId = streamId)
        )
    }

    private fun createJsonConverter(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    private fun createRepository(type: MessengerDaoStub.Type): WebRepository {
        val messengerDao = MessengerDaoStub(messagesGenerator, type)
        MessengerDaoProviderImpl.value = messengerDao
        return WebRepositoryImpl(
            MessagesCache(MessengerDaoProviderImpl),
            MessengerDaoProviderImpl,
            ApiServiceProviderStub(),
            AppAuthKeeperImpl(),
            createJsonConverter(),
            mainDispatcherRule.testDispatcher
        )
    }
}