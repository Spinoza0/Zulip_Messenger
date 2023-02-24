package com.spinoza.homework_1.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ContactTest {
    @Test
    fun contactsEqualTest() {
        val contact1 = Contact("John", "123")
        val contact2 = Contact("Mary", "456")
        val contact3 = Contact("John", "123")
        assertEquals(contact1, contact3)
        assertNotEquals(contact1, contact2)
    }
}