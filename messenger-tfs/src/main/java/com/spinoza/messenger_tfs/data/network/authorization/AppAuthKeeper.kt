package com.spinoza.messenger_tfs.data.network.authorization

interface AppAuthKeeper {

    fun getKey(): String

    fun setData(data: String)

    fun getValue(): String
}