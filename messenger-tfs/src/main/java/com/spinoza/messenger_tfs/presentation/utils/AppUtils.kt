package com.spinoza.messenger_tfs.presentation.utils

fun Throwable.getErrorText(): String = localizedMessage ?: message ?: toString()
