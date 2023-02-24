package com.spinoza.homework_1.presentation.utils

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinoza.homework_1.domain.Contact
import com.spinoza.homework_1.domain.ContactsList
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UtilsTest {

    @Test
    fun whenGetContactsListFromIntentResultIsEqualToSourceValue() {
        val contactsList = ContactsList(
            listOf(Contact("John", "123"), Contact("Mary", "456"))
        )

        val intent = Intent().putExtra(Constants.EXTRA_CONTACTS_LIST, contactsList)
        val result = getContactsListFromIntent(intent)

        assertEquals(contactsList, result)
    }

    @Test
    fun whenGetContactsListFromIntentReturnedValueIsDifferent() {
        val contactsList1 = ContactsList(
            listOf(Contact("John", "123"), Contact("Mary", "456"))
        )
        val contactsList2 = ContactsList(
            listOf(Contact("Peter", "123"), Contact("Jack", "456"))
        )

        val intent1 = Intent().putExtra(Constants.EXTRA_CONTACTS_LIST, contactsList1)
        val result1 = getContactsListFromIntent(intent1)

        assertNotEquals(contactsList2, result1)
    }

    @Test(expected = RuntimeException::class)
    fun getContactsListFromIntentThrowsRuntimeExceptionForEmptyIntent() {
        val intent = Intent()
        getContactsListFromIntent(intent)
    }
}