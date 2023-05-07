package com.spinoza.messenger_tfs.domain.model.event

enum class EventType {
    PRESENCE,
    CHANNEL,
    MESSAGE,
    UPDATE_MESSAGE,
    DELETE_MESSAGE,
    REACTION
}