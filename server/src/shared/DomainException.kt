package com.restpartijen.api.shared

/**
 * Base class for every domain error. StatusPages (GI-4, Stefan) maps each
 * subclass to one HTTP status code, so services never build error responses themselves.
 *
 * @param message the text StatusPages puts in the error response
 */
abstract class DomainException(message: String) : RuntimeException(message)

/**
 * 400 Bad Request — structural error in the request.
 *
 * @param message the text StatusPages puts in the error response
 * @property field the offending field, sent along in the error response; null when no single field is to blame
 */
class ValidationException(message: String, val field: String? = null) : DomainException(message)

/**
 * 422 Unprocessable Entity — well-formed request that breaks a domain rule.
 *
 * @param message the text StatusPages puts in the error response
 * @property field the field that breaks the rule, sent along in the error response;
 *   null when no single field is to blame
 */
class DomainRuleException(message: String, val field: String? = null) : DomainException(message)

/** 404 Not Found — the requested resource does not exist. */
class NotFoundException(message: String) : DomainException(message)

/** 401 Unauthorized — missing or invalid token. */
class UnauthorizedException(message: String) : DomainException(message)

/** 403 Forbidden — valid token, but wrong role or not the owner. */
class ForbiddenException(message: String) : DomainException(message)

/** 409 Conflict — status transition not allowed. */
class IllegalStateTransitionException(message: String) : DomainException(message)

/** 409 Conflict — the request clashes with existing data, e.g. registering an e-mail address that is taken (#47). */
class ConflictException(message: String) : DomainException(message)
