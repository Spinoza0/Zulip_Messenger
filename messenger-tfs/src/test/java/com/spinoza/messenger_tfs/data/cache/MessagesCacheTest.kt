package com.spinoza.messenger_tfs.data.cache

import com.spinoza.messenger_tfs.data.network.model.message.ReactionDto
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesPageType
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.stub.AuthorizationStorageStub
import com.spinoza.messenger_tfs.stub.MessagesGenerator
import com.spinoza.messenger_tfs.stub.MessengerDaoStub
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

class MessagesCacheTest {

    private val messagesGenerator = MessagesGenerator()
    private val ownUserId = 0L

    @Before
    fun setUp() {
        messagesGenerator.reset()
    }

    @Test
    fun `should messagesCache is empty after creating`() = runTest {
        val messagesCache = createEmptyMessagesCache()

        assertNotEquals(true, messagesCache.isNotEmpty())
    }


    @Test
    fun `should messagesCache not empty after adding one message`() = runTest {
        val messagesCache = createEmptyMessagesCache()
        val message = messagesGenerator.getNextMessageDto()

        messagesCache.add(message, false, provideMessagesFilter())

        assertEquals(true, messagesCache.isNotEmpty())
    }

    @Test
    fun `should messagesCache not empty after adding list of messages`() = runTest {
        val messagesCache = createEmptyMessagesCache()
        val messages = messagesGenerator.getListOfMessagesDto()
        val messagesPageType = provideMessagePageType()

        messagesCache.addAll(messages, messagesPageType, provideMessagesFilter())

        assertEquals(true, messagesCache.isNotEmpty())
    }

    @Test
    fun `should messagesCache is empty after reload`() = runTest {
        val messagesCache = createEmptyMessagesCache()
        val message = messagesGenerator.getNextMessageDto()

        messagesCache.add(message, false, provideMessagesFilter())
        messagesCache.reload()

        assertNotEquals(true, messagesCache.isNotEmpty())
    }

    @Test
    fun `should updateReaction changes reactions`() = runTest {
        val filter = provideMessagesFilter()
        val messagesCache = createNotEmptyMessagesCache(filter)
        val id = messagesGenerator.getLastId()
        val messagesBefore = messagesCache.getMessages(filter)
        val reactionsBefore = messagesBefore.find { it.user.userId == id }?.reactions
        val newReactionDto = createReactionDto()

        messagesCache.updateReaction(messagesGenerator.getLastId(), ownUserId, newReactionDto)
        val messagesAfter = messagesCache.getMessages(filter)
        val reactionsAfter = messagesAfter.find { it.user.userId == id }?.reactions

        assertNotEquals(reactionsBefore, reactionsAfter)
    }

    @Test
    fun `should getMessages returns empty list after creating messagesCache`() = runTest {
        val messagesCache = createEmptyMessagesCache()

        val messages = messagesCache.getMessages(provideMessagesFilter())

        assertEquals(true, messages.isEmpty())
    }

    @Test
    fun `should getMessages returns not empty list after adding messages`() = runTest {
        val messagesCache = createEmptyMessagesCache()
        val filter = provideMessagesFilter()

        messagesCache.addAll(
            messagesGenerator.getListOfMessagesDto(),
            provideMessagePageType(),
            filter
        )

        val messages = messagesCache.getMessages(filter)
        assertEquals(true, messages.isNotEmpty())
    }

    @Test
    fun `should remove method removes the message from messagesCache`() = runTest {
        val filter = provideMessagesFilter()
        val messagesCache = createNotEmptyMessagesCache(filter)
        val messagesBefore = messagesCache.getMessages(filter)
        val messagesSizeBefore = messagesBefore.size
        val lastMessageId = messagesBefore.last().id

        messagesCache.remove(lastMessageId)
        val messagesAfter = messagesCache.getMessages(filter)
        val messagesSizeAfter = messagesAfter.size

        assertNotEquals(messagesSizeAfter, messagesSizeBefore)
        assertEquals(null, messagesAfter.find { it.id == lastMessageId })
    }

    @Test
    fun `should getFirstMessageId returns id of the first message`() = runTest {
        val filter = provideMessagesFilter()
        val messagesCache = createNotEmptyMessagesCache(filter)
        val messages = messagesCache.getMessages(filter)
        val rawFirstMessageId = messages.first().id

        val firstMessageId = messagesCache.getFirstMessageId(filter)

        assertEquals(firstMessageId, rawFirstMessageId)
    }

    @Test
    fun `should getLastMessageId returns id of the last message`() = runTest {
        val filter = provideMessagesFilter()
        val messagesCache = createNotEmptyMessagesCache(filter)
        val messages = messagesCache.getMessages(filter)
        val rawLastMessageId = messages.last().id

        val lastMessageId = messagesCache.getLastMessageId(filter)

        assertEquals(lastMessageId, rawLastMessageId)
    }

    @Test
    fun `should result of the getLastMessageId not equals result of the getFirstMessageId`() =
        runTest {
            val filter = provideMessagesFilter()
            val messagesCache = createNotEmptyMessagesCache(filter)

            val messages = messagesCache.getMessages(filter)
            val firstMessageId = messagesCache.getFirstMessageId(filter)
            val lastMessageId = messagesCache.getLastMessageId(filter)

            assertEquals(true, messages.size > 1)
            assertNotEquals(firstMessageId, lastMessageId)
        }

    private fun createEmptyMessagesCache(): MessagesCache {
        return MessagesCacheImpl(
            MessengerDaoStub(messagesGenerator, MessengerDaoStub.Type.EMPTY),
            AuthorizationStorageStub()
        )
    }

    private fun createNotEmptyMessagesCache(filter: MessagesFilter): MessagesCache = runBlocking {
        val messengerDao = MessengerDaoStub(messagesGenerator, MessengerDaoStub.Type.EMPTY)
        val messagesCache = MessagesCacheImpl(messengerDao, AuthorizationStorageStub())
        messagesCache.addAll(
            messagesGenerator.getListOfMessagesDto(),
            provideMessagePageType(),
            filter
        )
        messagesCache
    }

    private fun createReactionDto(): ReactionDto {
        return ReactionDto(
            emojiName = "smiley",
            emojiCode = "1f603",
            reactionType = ReactionDto.REACTION_TYPE_UNICODE_EMOJI,
            userId = messagesGenerator.getLastId()
        )
    }

    private fun provideMessagesFilter(): MessagesFilter {
        val streamId = messagesGenerator.getStreamId()
        val topicName = messagesGenerator.getTopicName()
        return MessagesFilter(
            channel = Channel(channelId = streamId),
            topic = Topic(name = topicName, channelId = streamId)
        )
    }

    private fun provideMessagePageType() = MessagesPageType.STORED
}