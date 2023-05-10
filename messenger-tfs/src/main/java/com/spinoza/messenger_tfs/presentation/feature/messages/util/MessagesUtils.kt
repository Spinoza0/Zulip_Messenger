package com.spinoza.messenger_tfs.presentation.feature.messages.util

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessageDraft

fun MessageDraft.isReadyToSend(messagesFilter: MessagesFilter): Boolean {
    val topicNameBlank = messagesFilter.topic.name.isBlank()
    val contentNotBlank = content.isNotBlank()
    val subjectNotBlank = subject.isNotBlank()
    return (topicNameBlank && contentNotBlank) ||
            (!topicNameBlank && contentNotBlank && subjectNotBlank)
}