package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.cache.MessagesCache
import com.spinoza.messenger_tfs.data.database.MessengerDao
import com.spinoza.messenger_tfs.data.network.AppAuthKeeperImpl
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.repository.MessengerRepository
import com.spinoza.messenger_tfs.stub.ApiServiceStub
import com.spinoza.messenger_tfs.stub.AttachmentHandlerStub
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
class MessengerRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val messagesGenerator = MessagesGenerator()

    @Before
    fun setUp() {
        messagesGenerator.reset()
    }

    @Test
    fun `getStoredMessages returns empty list of messages`() = runTest {
        val messengerDao = MessengerDaoStub(messagesGenerator, MessengerDaoStub.Type.EMPTY)
        val repository = createRepository(messengerDao)
        val messagesFilter = provideMessagesFilter(messagesGenerator)

        val result = repository.getStoredMessages(messagesFilter)

        assertEquals(true, result.getOrNull()?.messages?.isEmpty())
    }

    @Test
    fun `getStoredMessages returns not empty list of messages`() = runTest {
        val messengerDao = MessengerDaoStub(messagesGenerator, MessengerDaoStub.Type.WITH_MESSAGES)
        val repository = createRepository(messengerDao)
        val messagesFilter = provideMessagesFilter(messagesGenerator)

        val result = repository.getStoredMessages(messagesFilter)

        assertEquals(true, result.getOrNull()?.messages?.isNotEmpty())
    }

    @Test
    fun `getStoredMessages returns error`() = runTest {
        val messengerDao =
            MessengerDaoStub(messagesGenerator, MessengerDaoStub.Type.WITH_GET_MESSAGES_ERROR)
        val repository = createRepository(messengerDao)
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

    private fun createRepository(messengerDao: MessengerDao): MessengerRepository {
        return MessengerRepositoryImpl(
            MessagesCache(messengerDao), messengerDao, ApiServiceStub(), AppAuthKeeperImpl(),
            createJsonConverter(), AttachmentHandlerStub(), mainDispatcherRule.testDispatcher
        )
    }
}