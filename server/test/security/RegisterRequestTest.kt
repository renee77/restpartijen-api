package com.restpartijen.api.security

import com.restpartijen.api.security.dto.RegisterRequest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** RegisterRequest.toString names the class and keeps the password out of logs. */
class RegisterRequestTest {
    @Test
    fun `toString names RegisterRequest and masks the password`() {
        // Arrange: a request with a recognizable password
        val request = RegisterRequest(
            email = "test@mail.com",
            password = "SuperSecret123",
            displayName = "TestName",
        )

        // Act
        val text = request.toString()

        // Assert: the right class, the password only as ***, the other fields still readable.
        // The messages leave out the text, so a failure does not print the test password.
        assertTrue(text.startsWith("RegisterRequest("), "toString does not start with RegisterRequest(")
        assertTrue(text.contains("password=***"), "toString does not show password=***")
        assertFalse(text.contains("SuperSecret123"), "toString leaked the password")
        assertTrue(text.contains("test@mail.com"), "toString does not show the email")
        assertTrue(text.contains("TestName"), "toString does not show the displayName")
    }
}
