package com.spinoza.messenger_tfs.data.network

import com.spinoza.messenger_tfs.domain.network.ApiServiceProvider

object ApiServiceProviderImpl : ApiServiceProvider {

    override lateinit var value: ZulipApiService
}