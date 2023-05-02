package com.spinoza.messenger_tfs.domain.network

interface AppAuthKeeper {

    fun getKey(): String

    fun setData(data: String)

    fun getValue(): String
}