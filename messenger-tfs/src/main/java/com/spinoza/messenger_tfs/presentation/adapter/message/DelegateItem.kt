package com.spinoza.messenger_tfs.presentation.adapter.message

interface DelegateItem {

    fun content(): Any

    fun id(): Long

    fun compareToOther(other: DelegateItem): Boolean
}