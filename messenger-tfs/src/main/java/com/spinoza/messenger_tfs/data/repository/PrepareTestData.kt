package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.domain.model.ReactionParam
import com.spinoza.messenger_tfs.domain.utils.emojiSet

// for testing purpose
fun prepareTestData(): List<Message> {
    val messages = mutableListOf<Message>()

    var count = 5
    val reactions = mutableMapOf<String, ReactionParam>()
    for (emoji in emojiSet) {
        reactions[emoji.toString()] = ReactionParam(listOf(count--), false)
        if (count <= 0) break
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