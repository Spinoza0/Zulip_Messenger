package com.spinoza.messenger_tfs.data.network

import com.bumptech.glide.load.model.LazyHeaders
import com.spinoza.messenger_tfs.data.network.authorization.AppAuthKeeper
import com.spinoza.messenger_tfs.di.BaseUrl
import com.spinoza.messenger_tfs.domain.network.WebUtil
import javax.inject.Inject

class WebUtilImpl @Inject constructor(
    private val authKeeper: AppAuthKeeper,
    @BaseUrl private val baseUrl: String,
) : WebUtil {

    private val urlUserUploadsPrefix = "${baseUrl}/user_uploads"

    override fun getFullUrl(url: String): String =
        if (url.startsWith(URL_HTTP_SECURED_PREFIX, ignoreCase = true) ||
            url.startsWith(URL_HTTP_BASIC_PREFIX, ignoreCase = true)
        ) {
            url
        } else {
            val modifiedUrl = if (url.startsWith(SLASH)) url else "$SLASH$url"
            "${baseUrl}$modifiedUrl"
        }

    override fun isUserUploadsUrl(url: String): Boolean =
        url.startsWith(urlUserUploadsPrefix, ignoreCase = true)

    override fun getLazyHeaders(): LazyHeaders =
        LazyHeaders.Builder()
            .addHeader(authKeeper.getKey(), authKeeper.getValue()).build()

    override fun getAttachmentsUrls(content: String): List<String> {
        val links = mutableSetOf<String>()
        val matches = urlRegex.findAll(content)
        for (match in matches) {
            val link = match.groupValues[FIRST_GROUP]
            if (link.isNotBlank()) {
                val fullUrl = getFullUrl(link)
                if (fullUrl.startsWith(baseUrl, ignoreCase = true)) {
                    links.add(fullUrl)
                }
            }
        }
        return links.toList()
    }

    private companion object {

        val urlRegex = """href="([^"]*)"""".toRegex()

        const val FIRST_GROUP = 1
        const val URL_HTTP_SECURED_PREFIX = "https://"
        const val URL_HTTP_BASIC_PREFIX = "http://"
        const val SLASH = "/"
    }
}