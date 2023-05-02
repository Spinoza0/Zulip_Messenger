package com.spinoza.messenger_tfs.data.network

import com.spinoza.messenger_tfs.BuildConfig
import com.spinoza.messenger_tfs.domain.network.BaseUrlProvider

object BaseUrlProviderImpl : BaseUrlProvider {

    override var value = BuildConfig.ZULIP_SERVER_URL
}