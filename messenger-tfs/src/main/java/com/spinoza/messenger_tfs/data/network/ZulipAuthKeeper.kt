package com.spinoza.messenger_tfs.data.network

import javax.inject.Inject

class ZulipAuthKeeper @Inject constructor(var authHeader: String)