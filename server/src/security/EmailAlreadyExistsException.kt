package com.restpartijen.api.security

/**
 * Thrown when an e-mail address is already registered.
 */
// TODO: temporary, replace by ConflictException once #47 is on main (remove before merge, B-27)
class EmailAlreadyExistsException(email: String) :
    RuntimeException("E-mail address already registered: $email")