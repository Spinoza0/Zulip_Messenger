package com.spinoza.homework_1.presentation.utils

import android.content.Intent
import android.os.Build
import com.spinoza.homework_1.domain.GetContactsResult
import com.spinoza.homework_1.presentation.utils.Constants.EXTRA_RESULT

fun getContactsResultFromIntent(intent: Intent): GetContactsResult {
    val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getParcelableExtra(EXTRA_RESULT, GetContactsResult::class.java)
    } else {
        @Suppress("deprecation")
        intent.getParcelableExtra(EXTRA_RESULT)
    }

    return result ?: throw RuntimeException("Parameter GetContactsResult not found in intent")
}