package com.spinoza.messenger_tfs.data

import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.model.MessageDto
import com.spinoza.messenger_tfs.data.model.ReactionParamDto
import com.spinoza.messenger_tfs.data.model.StreamDto
import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.domain.model.Topic
import com.spinoza.messenger_tfs.domain.utils.emojiSet
import kotlin.random.Random

// for testing purpose

val topics1 = listOf(
    Topic("jokes"),
    Topic("weather"),
)

val topics2 = listOf(
    Topic("testing"),
    Topic("development"),
)

val streamsDto = listOf(
    StreamDto(0L, "general", topics1),
    StreamDto(1L, "Development", topics2),
    StreamDto(2L, "Design", topics2),
    StreamDto(3L, "PR", topics1),
)

fun prepareTestData(): List<MessageDto> {
    val countOfReactions = 5
    val testUserId = 1L
    val messages = mutableListOf<MessageDto>()

    val reactions = emojiSet.take(countOfReactions).associate { emoji ->
        emoji.toString() to ReactionParamDto(listOf(testUserId))
    }

    repeat(20) { index ->
        val stream = streamsDto[Random.nextInt(streamsDto.size)]
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