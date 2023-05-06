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