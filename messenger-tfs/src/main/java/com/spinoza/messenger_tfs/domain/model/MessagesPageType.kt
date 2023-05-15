package com.spinoza.messenger_tfs.domain.model

enum class MessagesPageType {

    NEWEST,
    CURRENT_WITH_NEWEST,
    OLDEST,
    CURRENT_WITH_OLDEST,
    FIRST_UNREAD,
    LAST,
    STORED,
    AFTER_STORED
}