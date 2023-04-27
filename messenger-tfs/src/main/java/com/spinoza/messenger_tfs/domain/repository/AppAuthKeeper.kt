package com.spinoza.messenger_tfs.domain.repository

interface AppAuthKeeper {

    fun setData(data: String)

    fun getData(): String
}