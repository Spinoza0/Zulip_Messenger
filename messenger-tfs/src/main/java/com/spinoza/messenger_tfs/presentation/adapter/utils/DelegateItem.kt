package com.spinoza.messenger_tfs.presentation.adapter.utils

interface DelegateItem {

    fun content(): Any

    fun id(): Int

    fun compareToOther(other: DelegateItem): Boolean
}