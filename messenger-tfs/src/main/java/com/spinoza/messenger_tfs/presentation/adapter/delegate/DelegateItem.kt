package com.spinoza.messenger_tfs.presentation.adapter.delegate

interface DelegateItem {

    fun content(): Any

    fun id(): Int

    fun compareToOther(other: DelegateItem): Boolean
}