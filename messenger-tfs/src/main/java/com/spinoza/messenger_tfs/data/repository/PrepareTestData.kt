package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.domain.model.ReactionParam
import com.spinoza.messenger_tfs.domain.utils.emojiSet

// for testing purpose
fun prepareTestData(): List<Message> {
    val countOfReactions = 5
    val testUserId = 1

    val messages = mutableListOf<Message>()
    val reactions = emojiSet.take(countOfReactions).associate { emoji ->
        emoji.toString() to ReactionParam(listOf(testUserId), false)
    }

    repeat(20) { index ->
        val message = Message(
            MessageDate("${index % 2 + 1} марта 2023"),
            index,
            "User$index Name",
            "Message $index text",
            R.drawable.test_face,
            if (index % 3 == 0) reactions else emptyMap(),
            false,
            index
        )
        messages.add(message)
    }
    return messages
}