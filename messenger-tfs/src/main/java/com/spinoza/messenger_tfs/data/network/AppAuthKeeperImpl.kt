package com.spinoza.messenger_tfs.data.network

import com.spinoza.messenger_tfs.domain.network.AppAuthKeeper
import javax.inject.Inject

class AppAuthKeeperImpl @Inject constructor() : AppAuthKeeper {

    private var data = EMPTY_STRING

    override fun getKey(): String = HEADER_AUTHORIZATION

    override fun setData(data: String) {
        this.data = data
    }

    override fun getValue(): String = data

    private companion object {

        const val HEADER_AUTHORIZATION = "Authorization"
        const val EMPTY_STRING = ""
    }
}