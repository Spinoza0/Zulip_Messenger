package com.spinoza.messenger_tfs.stub

import com.spinoza.messenger_tfs.data.network.ZulipApiService
import com.spinoza.messenger_tfs.domain.network.ApiServiceProvider

class ApiServiceProviderStub : ApiServiceProvider {

    override var value: ZulipApiService
        get() = throw RuntimeException("Not yet implemented")
        set(_) {}
}