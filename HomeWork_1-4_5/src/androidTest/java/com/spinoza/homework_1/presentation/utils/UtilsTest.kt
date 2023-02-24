package com.spinoza.homework_1.presentation.utils

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spinoza.homework_1.domain.Contact
import com.spinoza.homework_1.domain.ContactsList
import com.spinoza.homework_1.domain.getContactsListFromIntent
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UtilsTest {

    @Test
    fun getContactsListFromIntentReturnCorrectObject() {
        val contactsList1 = ContactsList(
            listOf(Contact("John", "123"), Contact("Mary", "456"))
        )
        val contactsList2 = ContactsList(
            listOf(Contact("Peter", "123"), Contact("Jack", "456"))
        )

        val intent1 = Intent().putExtra(Constants.EXTRA_CONTACTS_LIST, contactsList1)
        val intent2 = Intent().putExtra(Constants.EXTRA_CONTACTS_LIST, contactsList2)

        val result1 = getContactsListFromIntent(intent1)
        val result2 = getContactsListFromIntent(intent2)

        assertEquals(contactsList1, result1)
        assertNotEquals(contactsList2, result1)

        assertEquals(contactsList2, result2)
        assertNotEquals(contactsList1, result2)
    }

    @Test(expected = RuntimeException::class)
    fun getContactsListFromIntentThrowsRuntimeExceptionForEmptyIntent() {
        val intent = Intent()
        getContactsListFromIntent(intent)
    }
}