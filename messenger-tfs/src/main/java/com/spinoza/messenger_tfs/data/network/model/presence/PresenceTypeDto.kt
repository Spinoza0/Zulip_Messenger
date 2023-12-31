package com.spinoza.messenger_tfs.data.network.model.presence

enum class PresenceTypeDto(val value: String) {
    ACTIVE("active"),
    IDLE("idle"),
    OFFLINE("offline")
}