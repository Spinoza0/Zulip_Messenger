package com.spinoza.messenger_tfs.data.network

import com.spinoza.messenger_tfs.di.BaseUrl
import com.spinoza.messenger_tfs.domain.network.WebUtil
import javax.inject.Inject

class WebUtilImpl @Inject constructor(@BaseUrl private val baseUrl: String) : WebUtil {

    private val urlUserUploadsPrefix = "${baseUrl}$PATH_USER_UPLOADS"

    override fun getFullUrl(url: String): String =
        if (url.startsWith(URL_HTTP_SECURED_PREFIX, ignoreCase = true) ||
            url.startsWith(URL_HTTP_BASIC_PREFIX, ignoreCase = true)
        ) {
            url
        } else {
            "${baseUrl}${getStringWithSlashAtStart(url)}"
        }

    override fun isUserUploadsUrl(url: String): Boolean =
        url.startsWith(urlUserUploadsPrefix, ignoreCase = true)

    override fun getAttachmentsUrls(content: String): List<String> {
        val links = mutableSetOf<String>()
        val matches = urlRegex.findAll(content)
        for (match in matches) {
            val link = match.groupValues[FIRST_GROUP]
            if (link.isNotBlank()) {
                val fullUrl = getFullUrl(link)
                if (fullUrl.startsWith(baseUrl, ignoreCase = true)) {
                    val linkWithSlashAtStart = getStringWithSlashAtStart(link)
                    val linkWithoutUploadsPath =
                        if (linkWithSlashAtStart.startsWith(PATH_USER_UPLOADS, ignoreCase = true)) {
                            linkWithSlashAtStart.drop(PATH_USER_UPLOADS.length)
                        } else {
                            linkWithSlashAtStart
                        }
                    links.add(linkWithoutUploadsPath)
                }
            }
        }
        return links.toList()
    }

    override fun getFileNameFromUrl(url: String, defaultName: String): String {
        return url.substringAfterLast(SLASH).ifBlank { defaultName }
    }

    override fun getStringWithSlashAtStart(url: String): String {
        return if (url.startsWith(SLASH)) url else "$SLASH$url"
    }

    override fun getStringWithoutSlashAtStart(url: String): String {
        return if (url.startsWith(SLASH)) url.drop(SLASH.length) else url
    }

    private companion object {

        val urlRegex = """href="([^"]*)"""".toRegex()

        const val FIRST_GROUP = 1
        const val URL_HTTP_SECURED_PREFIX = "https://"
        const val URL_HTTP_BASIC_PREFIX = "http://"
        const val PATH_USER_UPLOADS = "/user_uploads"
        const val SLASH = "/"
    }
}