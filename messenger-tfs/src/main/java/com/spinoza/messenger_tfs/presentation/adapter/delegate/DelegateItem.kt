package com.spinoza.messenger_tfs.presentation.adapter.delegate

interface DelegateItem {

    fun content(): Any

    fun id(): Long

    fun compareToOther(other: DelegateItem): Boolean
}