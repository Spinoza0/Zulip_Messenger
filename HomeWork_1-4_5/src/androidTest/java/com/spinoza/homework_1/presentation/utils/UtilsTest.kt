package com.spinoza.homework_1.presentation.utils

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinoza.homework_1.domain.Contact
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UtilsTest {

    @Test
    fun whenGetContactsListFromIntentResultIsEqualToSourceValue() {
        val contactsList =
            listOf(Contact("John", "123"), Contact("Mary", "456"))

        val intent = Intent().putParcelableArrayListExtra(
            Constants.EXTRA_CONTACTS_LIST,
            ArrayList(contactsList)
        )
        val result = getContactsListFromIntent(intent)

        assertEquals(contactsList, result)
    }

    @Test
    fun whenGetContactsListFromIntentReturnedValueIsDifferent() {
        val contactsList1 =
            listOf(Contact("John", "123"), Contact("Mary", "456"))
        val contactsList2 =
            listOf(Contact("Peter", "123"), Contact("Jack", "456"))

        val intent = Intent().putParcelableArrayListExtra(
            Constants.EXTRA_CONTACTS_LIST,
            ArrayList(contactsList1)
        )
        val result = getContactsListFromIntent(intent)

        assertNotEquals(contactsList2, result)
    }

    @Test(expected = RuntimeException::class)
    fun getContactsListFromIntentThrowsRuntimeExceptionForEmptyIntent() {
        val intent = Intent()
        getContactsListFromIntent(intent)
    }
}