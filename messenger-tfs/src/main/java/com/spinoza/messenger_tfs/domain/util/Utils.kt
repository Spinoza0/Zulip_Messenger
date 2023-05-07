package com.spinoza.messenger_tfs.domain.util

fun String.splitToWords(): List<String> {
    return split(" ")
}

fun String.isContainingWords(words: List<String>): Boolean {
    return words.all { word ->
        this.contains(word, true)
    }
}

fun Throwable.getText(): String = localizedMessage ?: message ?: toString()

fun getCurrentTimestamp(): Long = System.currentTimeMillis() / MILLIS_IN_SECOND

const val MILLIS_IN_SECOND = 1000L
const val SECONDS_IN_DAY = 24 * 60 * 60
const val EMPTY_STRING = ""