package com.spinoza.messenger_tfs.stub

import com.spinoza.messenger_tfs.data.network.model.message.MessageDto

class MessagesDtoStub {

    private var id = 0L
    private val streamId = 0L
    private val topicName = "topic"

    fun reset() {
        id = 0
    }

    fun getLastId() = id

    fun getStreamId() = streamId

    fun getTopicName() = topicName

    fun getNextMessage(): MessageDto {
        id++
        return MessageDto(
            id = id, streamId = streamId, senderId = id, content = "content",
            recipientId = id, timestamp = id, subject = topicName, isMeMessage = false,
            reactions = emptyList(), senderFullName = "senderFullName",
            senderEmail = "senderEmail", avatarUrl = null
        )
    }
}