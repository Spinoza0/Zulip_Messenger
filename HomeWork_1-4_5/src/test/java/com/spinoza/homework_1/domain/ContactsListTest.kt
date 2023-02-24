package com.spinoza.homework_1.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ContactsListTest {
    @Test
    fun contactsListEqualTest() {
        val contact1 = Contact("John", "123")
        val contact2 = Contact("Mary", "456")
        val contact3 = Contact("John", "123")
        val contactsList1 = ContactsList(listOf(contact1, contact2))
        val contactsList2 = ContactsList(listOf(contact2, contact1))
        val contactsList3 = ContactsList(listOf(contact1, contact3))
        val contactsList4 = ContactsList(listOf(contact1, contact2))
        assertEquals(contactsList1, contactsList4)
        assertNotEquals(contactsList1, contactsList2)
        assertNotEquals(contactsList2, contactsList3)
    }
}