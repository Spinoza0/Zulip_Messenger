package com.spinoza.messenger_tfs.domain.network

import com.spinoza.messenger_tfs.data.network.ZulipApiService

interface ApiServiceProvider {

    var value: ZulipApiService
}