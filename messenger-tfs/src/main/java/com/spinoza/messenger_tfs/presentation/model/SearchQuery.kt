package com.spinoza.messenger_tfs.presentation.model

data class SearchQuery(
    val screenPosition: Int,
    val text: String,
) {

    constructor(screenPosition: Int, text: CharSequence?) : this(screenPosition, text.toString())
}