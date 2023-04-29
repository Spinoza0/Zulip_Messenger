package com.spinoza.messenger_tfs.data.cache

import com.spinoza.messenger_tfs.data.network.model.message.MessageDto
import com.spinoza.messenger_tfs.data.network.model.message.ReactionDto
import com.spinoza.messenger_tfs.stub.MessengerDaoStub
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesPageType
import com.spinoza.messenger_tfs.domain.model.Topic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MessagesCacheTest {

    private var id = 0L
    private val streamId = 0L
    private val topicName = "topic"

    @Before
    fun setUp() {
        id = 0
    }

    @Test
    fun `messagesCache is empty after creating`() = runTest {
        val messagesCache = createEmptyMessagesCache()

        assertNotEquals(true, messagesCache.isNotEmpty())
    }


    @Test
    fun `messagesCache not empty after adding one message`() = runTest {
        val messagesCache = createEmptyMessagesCache()
        val message = createMessageDto()

        messagesCache.add(message, false)

        assertEquals(true, messagesCache.isNotEmpty())
    }

    @Test
    fun `messagesCache not empty after adding list of messages`() = runTest {
        val messagesCache = createEmptyMessagesCache()
        val messages = listOf(createMessageDto(), createMessageDto())
        val messagesPageType = provideMessagePageType()

        messagesCache.addAll(messages, messagesPageType)

        assertEquals(true, messagesCache.isNotEmpty())
    }

    @Test
    fun `messagesCache is empty after reload`() = runTest {
        val messagesCache = createEmptyMessagesCache()
        val message = createMessageDto()

        messagesCache.add(message, false)
        messagesCache.reload()

        assertNotEquals(true, messagesCache.isNotEmpty())
    }

    @Test
    fun `updateReaction changes reactions`() = runTest {
        val messagesCache = createNotEmptyMessagesCache()
        val messagesBefore = messagesCache.getMessages(provideMessagesFilter())
        val reactionsBefore = messagesBefore.find { it.senderId == id }?.reactions
        val newReactionDto = createReactionDto()

        messagesCache.updateReaction(id, id, newReactionDto)
        val messagesAfter = messagesCache.getMessages(provideMessagesFilter())
        val reactionsAfter = messagesAfter.find { it.senderId == id }?.reactions

        assertNotEquals(reactionsBefore, reactionsAfter)
    }

    @Test
    fun `getMessages returns empty list after creating messagesCache`() = runTest {
        val messagesCache = createEmptyMessagesCache()

        val messages = messagesCache.getMessages(provideMessagesFilter())

        assertEquals(true, messages.isEmpty())
    }

    @Test
    fun `getMessages returns not empty list after adding messages`() = runTest {
        val messagesCache = createEmptyMessagesCache()

        messagesCache.addAll(
            listOf(createMessageDto(), createMessageDto()),
            provideMessagePageType()
        )
        val messages = messagesCache.getMessages(provideMessagesFilter())

        assertEquals(true, messages.isNotEmpty())
    }

    @Test
    fun `remove method removes the message from messagesCache`() = runTest {
        val messagesCache = createNotEmptyMessagesCache()
        val messagesBefore = messagesCache.getMessages(provideMessagesFilter())
        val messagesSizeBefore = messagesBefore.size
        val lastMessageId = messagesBefore.last().id

        messagesCache.remove(lastMessageId)
        val messagesAfter = messagesCache.getMessages(provideMessagesFilter())
        val messagesSizeAfter = messagesAfter.size

        assertNotEquals(messagesSizeAfter, messagesSizeBefore)
        assertEquals(null, messagesAfter.find { it.id == lastMessageId })
    }

    @Test
    fun `getFirstMessageId returns id of the first message`() = runTest {
        val messagesCache = createNotEmptyMessagesCache()
        val messagesFilter = provideMessagesFilter()
        val messages = messagesCache.getMessages(messagesFilter)
        val rawFirstMessageId = messages.first().id

        val firstMessageId = messagesCache.getFirstMessageId(messagesFilter)

        assertEquals(firstMessageId, rawFirstMessageId)
    }

    @Test
    fun `getLastMessageId returns id of the last message`() = runTest {
        val messagesCache = createNotEmptyMessagesCache()
        val messagesFilter = provideMessagesFilter()
        val messages = messagesCache.getMessages(messagesFilter)
        val rawLastMessageId = messages.last().id

        val lastMessageId = messagesCache.getLastMessageId(messagesFilter)

        assertEquals(lastMessageId, rawLastMessageId)
    }

    @Test
    fun `result of the getLastMessageId not equals result of the getFirstMessageId`() =
        runTest {
            val messagesCache = createNotEmptyMessagesCache()
            val messagesFilter = provideMessagesFilter()

            val messages = messagesCache.getMessages(messagesFilter)
            val firstMessageId = messagesCache.getFirstMessageId(messagesFilter)
            val lastMessageId = messagesCache.getLastMessageId(messagesFilter)

            assertNotEquals(1, messages.size)
            assertNotEquals(firstMessageId, lastMessageId)
        }

    private fun createEmptyMessagesCache(): MessagesCache {
        return MessagesCache(MessengerDaoStub())
    }

    private fun createNotEmptyMessagesCache(): MessagesCache = runBlocking {
        val messagesCache = MessagesCache(MessengerDaoStub())
        messagesCache.addAll(
            listOf(createMessageDto(), createMessageDto(), createMessageDto()),
            provideMessagePageType()
        )
        messagesCache
    }

    private fun createMessageDto(): MessageDto {
        id++
        return MessageDto(
            id = id, streamId = streamId, senderId = id, content = "content",
            recipientId = id, timestamp = id, subject = topicName, isMeMessage = false,
            reactions = emptyList(), senderFullName = "senderFullName",
            senderEmail = "senderEmail", avatarUrl = null
        )
    }

    private fun createReactionDto(): ReactionDto {
        return ReactionDto(
            emojiName = "smiley", emojiCode = "1f603",
            reactionType = ReactionDto.REACTION_TYPE_UNICODE_EMOJI, userId = id
        )
    }

    private fun provideMessagesFilter(): MessagesFilter {
        return MessagesFilter(
            channel = Channel(channelId = streamId),
            topic = Topic(name = topicName, channelId = streamId)
        )
    }

    private fun provideMessagePageType() = MessagesPageType.STORED
}