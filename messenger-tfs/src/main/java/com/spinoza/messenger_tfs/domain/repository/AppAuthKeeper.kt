package com.spinoza.messenger_tfs.domain.repository

interface AppAuthKeeper {

    fun getKey(): String

    fun setData(data: String)

    fun getValue(): String
}