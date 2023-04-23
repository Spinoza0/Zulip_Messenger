package com.spinoza.messenger_tfs.domain.model

enum class MessagesAnchor(val value: String) {

    NEWEST("newest"),
    OLDEST("oldest"),
    FIRST_UNREAD("first_unread"),
    LAST("last");
}