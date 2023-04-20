package com.spinoza.messenger_tfs.presentation.feature.app.adapter

interface DelegateAdapterItem {

    fun content(): Any

    fun id(): Long

    fun compareToOther(other: DelegateAdapterItem): Boolean

    fun getChangePayload(newItem: DelegateAdapterItem): Any?
}