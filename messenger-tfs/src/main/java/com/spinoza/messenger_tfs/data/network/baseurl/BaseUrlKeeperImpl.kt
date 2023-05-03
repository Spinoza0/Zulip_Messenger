package com.spinoza.messenger_tfs.data.network.baseurl

import com.spinoza.messenger_tfs.BuildConfig

object BaseUrlKeeperImpl : BaseUrlKeeper {

    override var value = BuildConfig.ZULIP_SERVER_URL
}