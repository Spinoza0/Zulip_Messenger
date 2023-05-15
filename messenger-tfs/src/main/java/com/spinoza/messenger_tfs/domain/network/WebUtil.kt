package com.spinoza.messenger_tfs.domain.network

interface WebUtil {

    fun getFullUrl(url: String): String

    fun isUserUploadsUrl(url: String): Boolean

    fun getAttachmentsUrls(content: String): List<String>

    fun getFileNameFromUrl(url: String, defaultName: String): String

    fun getStringWithSlashAtStart(url: String): String

    fun getStringWithoutSlashAtStart(url: String): String
}