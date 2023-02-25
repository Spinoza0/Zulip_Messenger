package com.spinoza.homework_1.presentation.activity

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.spinoza.homework_1.domain.GetContactsResult
import com.spinoza.homework_1.presentation.utils.getContactsResultFromIntent

class GetContactsContract : ActivityResultContract<Unit?, GetContactsResult>() {

    override fun createIntent(context: Context, input: Unit?): Intent =
        GetContactsActivity.newIntent(context)

    override fun parseResult(resultCode: Int, intent: Intent?): GetContactsResult =
        if (intent != null) {
            getContactsResultFromIntent(intent)
        } else {
            throw RuntimeException("Parameter intent not found")
        }
}