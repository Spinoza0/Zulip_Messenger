package com.spinoza.messenger_tfs.domain.model

data class Emoji(
    val name: String,
    val code: String,
) {
    override fun toString(): String {
        return String(Character.toChars(code.toInt(16)))
    }
}