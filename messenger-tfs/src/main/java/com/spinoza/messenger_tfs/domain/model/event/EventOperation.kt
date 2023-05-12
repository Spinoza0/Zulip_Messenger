package com.spinoza.messenger_tfs.domain.model.event

enum class EventOperation(val value: String) {
    ADD("add"),
    REMOVE("remove"),
    CREATE("create"),
    DELETE("delete")
}