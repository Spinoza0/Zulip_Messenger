package com.spinoza.homework_1.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ContactTest {
    @Test
    fun contactsEqualTest() {
        val contact1 = Contact("John", "123")
        val contact2 = Contact("John", "123")
        assertEquals(contact1, contact2)
    }
}