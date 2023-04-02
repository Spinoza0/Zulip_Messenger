package com.spinoza.messenger_tfs.data.model.event

enum class EventTypeDto(val value: String) {
    PRESENCE("presence"),
    STREAM("stream"),
    MESSAGE("message"),
    REACTION("reaction")
}