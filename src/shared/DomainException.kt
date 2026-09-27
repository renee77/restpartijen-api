package com.restpartijen.api.shared

/**
 * Base class for every domain error. StatusPages (GI-4, Stefan) maps each
 * subclass to one HTTP status code, so services never build error responses themselves.
 */
abstract class DomainException(message: String) : RuntimeException(message)

/** 400 Bad Request — structural error in the request. [field] names the offending field. */
class ValidationException(message: String, val field: String? = null) : DomainException(message)

/** 422 Unprocessable Entity — well-formed request that breaks a domain rule. */
class DomainRuleException(message: String) : DomainException(message)

/** 404 Not Found — the requested resource does not exist. */
class NotFoundException(message: String) : DomainException(message)

/** 401 Unauthorized — missing or invalid token. */
class UnauthorizedException(message: String) : DomainException(message)

/** 403 Forbidden — valid token, but wrong role or not the owner. */
class ForbiddenException(message: String) : DomainException(message)

/** 409 Conflict — status transition not allowed. */
class IllegalStateTransitionException(message: String) : DomainException(message)