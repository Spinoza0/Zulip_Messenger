package com.spinoza.messenger_tfs.domain.utils

fun List<Int>.removeIfExistsOrAddToList(value: Int): List<Int> {
    val newList = mutableListOf<Int>()
    var isInList = false
    this.forEach { existingValue ->
        if (existingValue == value) {
            isInList = true
        } else {
            newList.add(existingValue)
        }
    }
    if (!isInList) {
        newList.add(value)
    }
    return newList
}