package com.restpartijen.api.security.model

import com.restpartijen.api.shared.Role

/**
 * The user making a request, as read from a verified token by currentUser().
 *
 * @property id the user id from the token
 * @property role the role from the token
 */
data class CurrentUser(val id: Long, val role: Role)
