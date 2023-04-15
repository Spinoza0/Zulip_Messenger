package com.spinoza.messenger_tfs.presentation.model.channels

data class SearchQuery(
    val screenPosition: Int,
    val text: String,
) {

    constructor(screenPosition: Int, text: CharSequence?) : this(screenPosition, text.toString())
}