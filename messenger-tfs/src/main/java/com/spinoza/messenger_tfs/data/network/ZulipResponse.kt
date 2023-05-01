package com.spinoza.messenger_tfs.data.network

import com.spinoza.messenger_tfs.data.network.ZulipApiService.Companion.RESULT_SUCCESS

interface ZulipResponse {

    val result: String
    val msg: String

    fun isSuccess(): Boolean = result == RESULT_SUCCESS
}