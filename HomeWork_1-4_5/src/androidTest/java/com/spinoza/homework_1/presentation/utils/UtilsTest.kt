package com.spinoza.homework_1.presentation.utils

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinoza.homework_1.domain.Contact
import com.spinoza.homework_1.domain.GetContactsResult
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UtilsTest {

    @Test
    fun whenGetContactsListFromIntentResultIsEqualToSourceValue() {
        val contacts = GetContactsResult.Success(
            listOf(Contact("John", "123"), Contact("Mary", "456"))
        )

        val intent = Intent().putExtra(Constants.EXTRA_RESULT, contacts)
        val result = getContactsResultFromIntent(intent)

        assertEquals(contacts, result)
    }

    @Test
    fun whenGetContactsListFromIntentReturnedValueIsDifferent() {
        val contacts1 = GetContactsResult.Success(
            listOf(Contact("John", "123"), Contact("Mary", "456"))
        )
        val contacts2 = GetContactsResult.Success(
            listOf(Contact("Peter", "123"), Contact("Jack", "456"))
        )

        val intent = Intent().putExtra(Constants.EXTRA_RESULT, contacts1)
        val result = getContactsResultFromIntent(intent)

        assertNotEquals(contacts2, result)
    }

    @Test(expected = RuntimeException::class)
    fun getContactsListFromIntentThrowsRuntimeExceptionForEmptyIntent() {
        val intent = Intent()
        getContactsResultFromIntent(intent)
    }
}