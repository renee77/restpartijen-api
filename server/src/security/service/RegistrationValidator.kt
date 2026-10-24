package com.restpartijen.api.security.service

import com.restpartijen.api.shared.DomainRuleException
import com.restpartijen.api.shared.ValidationException

object RegistrationValidator {
    // All the limits for the input
    const val EMAIL_MAX_LENGTH = 254
    const val PASSWORD_MIN_LENGTH = 8
    const val PASSWORD_MAX_LENGTH = 64
    const val DISPLAY_NAME_MAX_LENGTH = 50

    /** Validates registration input (A11). Stops at the first broken rule. */
    fun validate(email: String, password: String, displayName: String) {
        validateEmail(email.trim())
        validatePassword(password) // never trimmed
        validateDisplayName(displayName.trim())
    }

    private fun validateEmail(email: String) {
        if (email.isEmpty()) fail("email", "Email is required")
        if (email.length > EMAIL_MAX_LENGTH) {
            fail("email", "Email can be at most $EMAIL_MAX_LENGTH characters")
        }
        if (!isSimpleEmailFormat(email)) fail("email", "Email format is invalid")
    }

    private fun validatePassword(password: String) {
        if (password.length < PASSWORD_MIN_LENGTH) {
            fail("password", "Password must be at least $PASSWORD_MIN_LENGTH characters")
        }
        if (password.length > PASSWORD_MAX_LENGTH) {
            fail("password", "Password can be at most $PASSWORD_MAX_LENGTH characters")
        }
    }

    private fun validateDisplayName(displayName: String) {
        if (displayName.isEmpty()) fail("displayName", "Display name is required")
        if (displayName.length > DISPLAY_NAME_MAX_LENGTH) {
            fail("displayName", "Display name can be at most $DISPLAY_NAME_MAX_LENGTH characters")
        }
    }

    // Exactly one @, text before and after it, a dot inside the domain, no spaces.
    private fun isSimpleEmailFormat(email: String): Boolean {
        if (email.any { it.isWhitespace() }) return false
        if (email.count { it == '@' } != 1) return false
        val (local, domain) = email.split('@')
        if (local.isEmpty() || domain.isEmpty()) return false
        val dot = domain.indexOf('.')
        return dot > 0 && dot < domain.length - 1
    }

    private fun fail(field: String, message: String): Nothing = throw ValidationException(message, field)
}
