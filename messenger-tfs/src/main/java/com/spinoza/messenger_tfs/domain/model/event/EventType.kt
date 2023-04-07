package com.spinoza.messenger_tfs.domain.model.event

enum class EventType {
    PRESENCE,
    CHANNEL,
    MESSAGE,
    DELETE_MESSAGE,
    REACTION
}