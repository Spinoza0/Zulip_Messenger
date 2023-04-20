package org.kimp.tfs.hw7.utils

fun String.extractInitials() =
    this.split(" ")
        .asSequence()
        .filter { word -> word.isNotEmpty() }
        .map { word -> word[0].titlecaseChar() }
        .take(2)
        .joinToString(separator = "")
