package com.spinoza.messenger_tfs.presentation.adapter.message

interface DelegateAdapterItem {

    fun content(): Any

    fun id(): Long

    fun compareToOther(other: DelegateAdapterItem): Boolean
}