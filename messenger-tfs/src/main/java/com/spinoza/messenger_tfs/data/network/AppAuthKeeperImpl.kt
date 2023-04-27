package com.spinoza.messenger_tfs.data.network

import com.spinoza.messenger_tfs.domain.repository.AppAuthKeeper
import javax.inject.Inject

class AppAuthKeeperImpl @Inject constructor() : AppAuthKeeper {

    private var data = EMPTY_STRING

    override fun setData(data: String) {
        this.data = data
    }

    override fun getData(): String = data

    private companion object {

        const val EMPTY_STRING = ""
    }
}