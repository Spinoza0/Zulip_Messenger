package com.spinoza.messenger_tfs.data.network

import com.bumptech.glide.load.model.LazyHeaders
import com.spinoza.messenger_tfs.BuildConfig
import com.spinoza.messenger_tfs.domain.repository.AppAuthKeeper
import com.spinoza.messenger_tfs.domain.webutil.WebUtil
import javax.inject.Inject

class WebUtilImpl @Inject constructor(private val authKeeper: AppAuthKeeper) : WebUtil {

    override fun getFullUrl(url: String): String =
        if (url.startsWith(URL_HTTP_SECURED_PREFIX, ignoreCase = true) ||
            url.startsWith(URL_HTTP_BASIC_PREFIX, ignoreCase = true)
        ) {
            url
        } else {
            val modifiedUrl = if (!url.startsWith(URL_SLASH)) {
                "/$url"
            } else {
                url
            }
            "$BASE_URL$modifiedUrl"
        }


    override fun isUserUploadsUrl(url: String): Boolean =
        url.startsWith(URL_USER_UPLOADS_PREFIX, ignoreCase = true)

    override fun getLazyHeaders(): LazyHeaders =
        LazyHeaders.Builder()
            .addHeader(HEADER_AUTHORIZATION_TITLE, authKeeper.getData()).build()

    private companion object {
        const val BASE_URL = BuildConfig.ZULIP_SERVER_URL
        const val URL_HTTP_SECURED_PREFIX = "https://"
        const val URL_HTTP_BASIC_PREFIX = "http://"
        const val URL_SLASH = "/"
        const val URL_USER_UPLOADS_PREFIX = "$BASE_URL/user_uploads"
        const val HEADER_AUTHORIZATION_TITLE = "Authorization"
    }
}