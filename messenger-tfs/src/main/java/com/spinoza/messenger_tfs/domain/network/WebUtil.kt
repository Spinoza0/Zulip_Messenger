package com.spinoza.messenger_tfs.domain.network

import com.bumptech.glide.load.model.LazyHeaders

interface WebUtil {

    fun getFullUrl(url: String): String

    fun isUserUploadsUrl(url: String): Boolean

    fun getLazyHeaders(): LazyHeaders

    fun getAttachmentsUrls(content: String): List<String>
}