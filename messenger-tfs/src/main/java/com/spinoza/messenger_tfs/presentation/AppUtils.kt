package com.spinoza.messenger_tfs.presentation

fun Throwable.getErrorText(): String = localizedMessage ?: message ?: toString()
