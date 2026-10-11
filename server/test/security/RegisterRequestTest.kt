package com.restpartijen.api.security

import com.restpartijen.api.security.dto.RegisterRequest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RegisterRequestTest {
    @Test
    fun `toString does not show the password`() {
        // Arrange: a request with a recognizable password
        val request = RegisterRequest(
            email = "test@mail.com",
            password = "SuperSecret123",
            displayName = "TestName",
        )

        // Act
        val text = request.toString()

        // Assert: the password is hidden, the other fields are still readable
        assertFalse(text.contains("SuperSecret123"))
        assertTrue(text.contains("test@mail.com"))
    }
}