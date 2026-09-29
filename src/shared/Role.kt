package com.restpartijen.api.shared

/**
 * The role of a user, stored as the `role` claim in the JWT (GI-3, decisions 3.2 and 3.3).
 *
 * A user has exactly one role. Roles are not hierarchical: an ADMIN is not a SUPPLIER with extra rights.
 * Registration always creates a COLLECTOR (decision 3.7). Owner: Lonneke.
 */
enum class Role {
    /** A food business that lists and manages its own surplus products. */
    SUPPLIER,

    /** A consumer who searches, reserves and collects surplus products. */
    COLLECTOR,

    /** A platform administrator who oversees and removes listings. */
    ADMIN,
}