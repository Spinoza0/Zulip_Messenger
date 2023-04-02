package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.model.message.MessageDto
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import java.util.*

class MessagesCache {

    private val data = TreeSet<MessageDto>()

    fun add(messageDto: MessageDto) {
        data.add(messageDto)
    }

    fun addAll(messagesDto: List<MessageDto>) {
        data.addAll(messagesDto)
    }

    fun getMessages(messagesFilter: MessagesFilter): List<MessageDto> {
        return data
            .filter {
                if (messagesFilter.channel.channelId != Channel.UNDEFINED_ID)
                    messagesFilter.channel.channelId == it.streamId
                else true
            }
            .filter {
                if (messagesFilter.topic.name.isNotEmpty())
                    messagesFilter.topic.name == it.subject
                else true
            }
    }
}