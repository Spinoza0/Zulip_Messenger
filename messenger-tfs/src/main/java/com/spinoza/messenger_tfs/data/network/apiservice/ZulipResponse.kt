package com.spinoza.messenger_tfs.data.network.apiservice

import com.spinoza.messenger_tfs.data.network.apiservice.ZulipApiService.Companion.RESULT_SUCCESS

@Suppress("unused")
interface ZulipResponse {

    val result: String
    val msg: String

    fun isSuccess(): Boolean = result == RESULT_SUCCESS
}