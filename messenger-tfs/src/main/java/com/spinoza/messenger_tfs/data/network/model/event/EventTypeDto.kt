package com.spinoza.messenger_tfs.data.network.model.event

enum class EventTypeDto(val value: String) {
    PRESENCE("presence"),
    STREAM("stream"),
    MESSAGE("message"),
    DELETE_MESSAGE("delete_message"),
    REACTION("reaction")
}