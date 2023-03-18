package com.spinoza.messenger_tfs.data

import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.model.ChannelDto
import com.spinoza.messenger_tfs.data.model.MessageDto
import com.spinoza.messenger_tfs.data.model.ReactionParamDto
import com.spinoza.messenger_tfs.data.model.TopicDto
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.domain.utils.emojiSet
import kotlin.random.Random

// for testing purpose

val topics1 = listOf(
    TopicDto("jokes", Message.UNDEFINED_ID),
    TopicDto("weather", Message.UNDEFINED_ID),
)

val topics2 = listOf(
    TopicDto("testing", Message.UNDEFINED_ID),
    TopicDto("development", Message.UNDEFINED_ID),
)

val channelsDto = listOf(
    ChannelDto(0L, "general", topics1),
    ChannelDto(1L, "Development", topics2),
    ChannelDto(2L, "Design", topics2),
    ChannelDto(3L, "PR", topics1),
)

fun prepareTestData(): List<MessageDto> {
    val countOfReactions = 5
    val testUserId = 1L
    val messages = mutableListOf<MessageDto>()

    val reactions = emojiSet.take(countOfReactions).associate { emoji ->
        emoji.toString() to ReactionParamDto(listOf(testUserId))
    }

    repeat(20) { index ->
        val stream = channelsDto[Random.nextInt(channelsDto.size)]
        val topic = stream.topics[Random.nextInt(stream.topics.size)]
        val message = MessageDto(
            index.toLong(),
            MessageDate("${index % 2 + 1} марта 2023"),
            index.toLong(),
            "User$index Name",
            "Message $index text",
            R.drawable.test_face,
            if (index % 3 == 0) reactions else emptyMap(),
            stream.id,
            topic.name
        )
        messages.add(message)
    }
    return messages
}