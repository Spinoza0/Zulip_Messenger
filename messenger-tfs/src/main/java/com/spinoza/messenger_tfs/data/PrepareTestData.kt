package com.spinoza.messenger_tfs.data

import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.data.model.MessageDto
import com.spinoza.messenger_tfs.data.model.ReactionParamDto
import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.domain.utils.emojiSet

// for testing purpose
fun prepareTestData(): List<MessageDto> {
    val countOfReactions = 5
    val testUserId = 1L

    val messages = mutableListOf<MessageDto>()
    val reactions = emojiSet.take(countOfReactions).associate { emoji ->
        emoji.toString() to ReactionParamDto(listOf(testUserId))
    }

    repeat(20) { index ->
        val message = MessageDto(
            MessageDate("${index % 2 + 1} марта 2023"),
            index.toLong(),
            "User$index Name",
            "Message $index text",
            R.drawable.test_face,
            if (index % 3 == 0) reactions else emptyMap(),
            index.toLong()
        )
        messages.add(message)
    }
    return messages
}