package com.spinoza.messenger_tfs.presentation.feature.channels.model

data class SearchQuery(
    val screenPosition: Int,
    val text: String,
) {

    constructor(screenPosition: Int, text: CharSequence?) : this(screenPosition, text.toString())
}