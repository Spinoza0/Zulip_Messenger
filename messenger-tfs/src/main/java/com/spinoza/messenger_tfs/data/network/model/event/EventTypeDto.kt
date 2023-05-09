package com.spinoza.messenger_tfs.data.network.model.event

enum class EventTypeDto(val value: String) {
    PRESENCE("presence"),
    STREAM("stream"),
    MESSAGE("message"),
    UPDATE_MESSAGE("update_message"),
    DELETE_MESSAGE("delete_message"),
    REACTION("reaction")
}