package com.restpartijen.api.security

import com.restpartijen.api.security.repository.ExposedUserRepository
import com.restpartijen.api.security.repository.UserRepository
import com.restpartijen.api.security.service.JwtConfig
import com.restpartijen.api.security.service.JwtSettings
import com.restpartijen.api.security.service.LoginService
import com.restpartijen.api.security.service.PasswordHasher
import com.restpartijen.api.security.service.RegistrationService
import com.restpartijen.api.security.service.configureSecurity
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.di.provide

/**
 * Everything GI-3 needs, in one call from Application.module() (decision 3.9):
 * registers the security classes in the DI and installs the Authentication plugin.
 *
 * It takes JwtSettings (a security type), not the config package's JwtProperties,
 * so the security package does not know the config package. module() does the mapping.
 * Call it after configureDependencies() and before the routes.
 */
fun Application.configureSecurityModule(jwtSettings: JwtSettings) {
    dependencies {
        provide<JwtSettings> { jwtSettings }
        provide(::JwtConfig)
        provide<UserRepository>(::ExposedUserRepository)
        provide(::PasswordHasher)
        provide(::RegistrationService)
        provide(::LoginService)
    }

    // The same JwtConfig that LoginService gets, so issuing and verifying always match.
    val jwtConfig: JwtConfig by dependencies
    configureSecurity(jwtConfig)
}