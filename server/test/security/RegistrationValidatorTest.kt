package com.restpartijen.api.security

import com.restpartijen.api.security.service.RegistrationValidator
import com.restpartijen.api.shared.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** RegistrationValidator: the limits of every field, with the boundary values on both sides (A11). */
class RegistrationValidatorTest {

    // Password tests
    @Test
    fun `password of 7 characters is rejected`() {
        // Arrange: a password one character below the minimum, and otherwise valid input
        val tooShort = "a".repeat(RegistrationValidator.PASSWORD_MIN_LENGTH - 1)

        // Act + Assert: validation must throw, and the error points at the password field
        val exception = assertFailsWith<ValidationException> {
            RegistrationValidator.validate(
                email = "test@mail.com",
                password = tooShort,
                displayName = "TestName",
            )
        }
        assertEquals("password", exception.field)
    }

    @Test
    fun `password of 8 characters is accepted`() {
        // Arrange: a password that is exactly 8 characters
        val justRight = "justrigh"

        // Act + Assert: no exception means the input is valid
        RegistrationValidator.validate(
            email = "test@mail.com",
            password = justRight,
            displayName = "TestName",
        )
    }

    @Test
    fun `password of 64 characters is accepted`() {
        // Arrange: A password that is exactly 64 characters.
        val justRightLong = "a".repeat(RegistrationValidator.PASSWORD_MAX_LENGTH)

        // Act and Assert: No exception
        RegistrationValidator.validate(
            email = "test@mail.com",
            password = justRightLong,
            displayName = "TesterTime",
        )
    }

    @Test
    fun `password of 65 characters is rejected`() {
        // Arrange: a password one character above the maximum, and otherwise valid input
        val tooLong = "a".repeat(RegistrationValidator.PASSWORD_MAX_LENGTH + 1)

        // Act + Assert: validation must throw, and the error points at the password field
        val exception = assertFailsWith<ValidationException> {
            RegistrationValidator.validate(
                email = "test@mail.com",
                password = tooLong,
                displayName = "TestName",
            )
        }
        assertEquals("password", exception.field)
    }

    @Test
    fun `password does not get trimmed`() {
        // Arrange: a password of 12 spaces; trimmed, it would be empty and too short
        val justSpaces = " ".repeat(12)

        // Act and Assert: No exception
        RegistrationValidator.validate(
            email = "test@mail.com",
            password = justSpaces,
            displayName = "TesterTime",
        )
    }

    // Email Tests
    @Test
    fun `blank e-mail is rejected`() {
        // Arrange: an email without any input, just blank
        val emptyMail = ""

        // Act + Assert: validation must throw, and the error points at the email field
        val exception = assertFailsWith<ValidationException> {
            RegistrationValidator.validate(
                email = emptyMail,
                password = "testPassword",
                displayName = "TestName",
            )
        }
        assertEquals("email", exception.field)
    }

    @Test
    fun `email without @ is rejected`() {
        // Arrange: an email without an @
        val mail = "NotReallyAMail.com"

        // Act + Assert: validation must throw, and the error points at the email field
        val exception = assertFailsWith<ValidationException> {
            RegistrationValidator.validate(
                email = mail,
                password = "testPassword",
                displayName = "TestName",
            )
        }
        assertEquals("email", exception.field)
    }

    @Test
    fun `email without dot is rejected`() {
        // Arrange: an email without a .
        val mail = "NotReally@AMail"

        // Act + Assert: validation must throw, and the error points at the email field
        val exception = assertFailsWith<ValidationException> {
            RegistrationValidator.validate(
                email = mail,
                password = "testPassword",
                displayName = "TestName",
            )
        }
        assertEquals("email", exception.field)
    }

    @Test
    fun `e-mail with spaces is rejected`() {
        // Arrange: an email with spaces
        val spaceMail = "ThisIs NotAMail"

        // Act + Assert: validation must throw, and the error points at the email field
        val exception = assertFailsWith<ValidationException> {
            RegistrationValidator.validate(
                email = spaceMail,
                password = "testPassword",
                displayName = "TestName",
            )
        }
        assertEquals("email", exception.field)
    }

    @Test
    fun `email of 254 characters is accepted`() {
        // Arrange: An email that is exactly 254 characters.
        val domain = "@mail.com"
        val justRightLong = "a".repeat(RegistrationValidator.EMAIL_MAX_LENGTH - domain.length) + domain

        // Act and Assert: No exception
        RegistrationValidator.validate(
            email = justRightLong,
            password = "TestPassword",
            displayName = "TesterTime",
        )
    }

    @Test
    fun `email of 255 characters is rejected`() {
        // Arrange: an email one character above the maximum, and otherwise valid input
        val tooLong = "testmail@word." + "a".repeat(RegistrationValidator.EMAIL_MAX_LENGTH - 13)

        // Act + Assert: validation must throw, and the error points at the email field
        val exception = assertFailsWith<ValidationException> {
            RegistrationValidator.validate(
                email = tooLong,
                password = "TestPassword",
                displayName = "TestName",
            )
        }
        assertEquals("email", exception.field)
    }

    // Username Tests
    @Test
    fun `blank username is rejected`() {
        // Arrange: a display name without any input, just blank
        val emptyName = ""

        // Act + Assert: validation must throw, and the error points at the displayName field
        val exception = assertFailsWith<ValidationException> {
            RegistrationValidator.validate(
                email = "test@email.com",
                password = "testPassword",
                displayName = emptyName,
            )
        }
        assertEquals("displayName", exception.field)
    }

    @Test
    fun `username of 51 characters is rejected`() {
        // Arrange: a username one character above the maximum, and otherwise valid input
        val tooLong = "a".repeat(RegistrationValidator.DISPLAY_NAME_MAX_LENGTH + 1)

        // Act + Assert: validation must throw, and the error points at the displayName field
        val exception = assertFailsWith<ValidationException> {
            RegistrationValidator.validate(
                email = "test@mailtest.com",
                password = "TestPassword",
                displayName = tooLong,
            )
        }
        assertEquals("displayName", exception.field)
    }

    @Test
    fun `username of 50 characters is accepted`() {
        // Arrange: a username one character at the maximum, and otherwise valid input
        val justRight = "a".repeat(RegistrationValidator.DISPLAY_NAME_MAX_LENGTH - 1)

        // Act and Assert: No exception
        RegistrationValidator.validate(
            email = "test@mailtest.com",
            password = "TestPassword",
            displayName = justRight,
        )
    }
}
