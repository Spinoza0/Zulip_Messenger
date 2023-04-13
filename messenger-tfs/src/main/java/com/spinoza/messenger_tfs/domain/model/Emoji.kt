package com.spinoza.messenger_tfs.domain.model

data class Emoji(
    val name: String,
    val code: String,
) {

    override fun toString(): String {
        return runCatching {
            val codeParts = code.split("-").map { it.toInt(BASE) }.toIntArray()
            String(codeParts, FIRST_PART, codeParts.size)
        }.getOrElse { ":$name:" }
    }

    private companion object {
        const val BASE = 16
        const val FIRST_PART = 0
    }
}