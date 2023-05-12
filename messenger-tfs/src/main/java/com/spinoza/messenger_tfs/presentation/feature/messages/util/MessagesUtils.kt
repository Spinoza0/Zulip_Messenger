package com.spinoza.messenger_tfs.presentation.feature.messages.util

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.presentation.feature.messages.model.MessageDraft

fun MessageDraft.isReadyToSend(messagesFilter: MessagesFilter): Boolean {
    val isTopicNameBlank = messagesFilter.topic.name.isBlank()
    val isContentNotBlank = content.isNotBlank()
    val isSubjectNotBlank = subject.isNotBlank()
    return (isTopicNameBlank && isContentNotBlank && isSubjectNotBlank) ||
            (!isTopicNameBlank && isContentNotBlank)
}