package com.spinoza.messenger_tfs.domain.util

import com.spinoza.messenger_tfs.domain.model.Topic

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

fun Topic.nameEquals(otherName: String): Boolean {
    return name.equals(otherName, true)
}

const val MILLIS_IN_SECOND = 1000L
const val EMPTY_STRING = ""