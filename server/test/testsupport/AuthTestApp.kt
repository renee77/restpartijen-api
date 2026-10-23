package com.restpartijen.api.testsupport

import com.restpartijen.api.plugins.configureSerialization
import com.restpartijen.api.plugins.configureStatusPages
import com.restpartijen.api.security.repository.ExposedUserRepository
import com.restpartijen.api.security.routes.configureAuthRouting
import com.restpartijen.api.security.service.JwtConfig
import com.restpartijen.api.security.service.LoginService
import com.restpartijen.api.security.service.PasswordHasher
import com.restpartijen.api.security.service.RegistrationService
import com.restpartijen.api.shared.SupplierIdLookup
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import kotlin.time.Instant

/** Test clock moment for registration. */
val authTestNow: Instant = Instant.parse("2026-01-15T10:30:00Z")

/**
 * Same plugin chain as production for the auth routes, with the real services on the test database.
 * Call createEmptyTestDatabase(UsersTable) first.
 */
fun Application.setUpAuthTestApp(
    jwtConfig: JwtConfig,
    supplierIdLookup: SupplierIdLookup = FakeSupplierIdLookup()

    ) {
    configureSerialization()
    configureStatusPages()
    dependencies {
        provide<RegistrationService> {
            RegistrationService(ExposedUserRepository(), PasswordHasher(), FixedClock(authTestNow))
        }
        provide<LoginService> {
            LoginService(ExposedUserRepository(), PasswordHasher(), jwtConfig, supplierIdLookup)
        }
    }
    configureAuthRouting()
}