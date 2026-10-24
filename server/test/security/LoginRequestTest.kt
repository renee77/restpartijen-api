package com.restpartijen.api.security

import com.restpartijen.api.security.dto.LoginRequest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** LoginRequest.toString names the class and keeps the password out of logs. */
class LoginRequestTest {
    @Test
    fun `toString names LoginRequest and masks the password`() {
        // Arrange: a request with a recognizable password
        val request = LoginRequest(
            email = "test@mail.com",
            password = "SuperSecret123",
        )

        // Act
        val text = request.toString()

        // Assert: the right class, the password only as ***, the email still readable.
        // The messages leave out the text, so a failure does not print the test password.
        assertTrue(text.startsWith("LoginRequest("), "toString does not start with LoginRequest(")
        assertTrue(text.contains("password=***"), "toString does not show password=***")
        assertFalse(text.contains("SuperSecret123"), "toString leaked the password")
        assertTrue(text.contains("test@mail.com"), "toString does not show the email")
    }
}
