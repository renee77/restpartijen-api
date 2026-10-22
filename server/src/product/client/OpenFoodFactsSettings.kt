package com.restpartijen.api.product.client

import io.ktor.server.config.ApplicationConfig

// One address, no spaces or control characters: it goes into a header, so a line break must never get in.
private val CONTACT_EMAIL = Regex("""[^\s@]+@[^\s@]+\.[^\s@]+""")

/**
 * The settings of the Open Food Facts client (§10.3, A5).
 *
 * @property contactEmail the address Open Food Facts can reach us on, sent in the User-Agent;
 *   it comes from the environment (OFF_CONTACT_EMAIL), so no personal address sits in the source (security review M-4)
 */
data class OpenFoodFactsSettings(val contactEmail: String)

/**
 * Reads the `openFoodFacts` section of application.yaml. Without a valid OFF_CONTACT_EMAIL the application
 * stops at startup with a clear message, like JWT_SECRET (B-52): Open Food Facts asks every app for a contact.
 */
fun ApplicationConfig.openFoodFactsSettings(): OpenFoodFactsSettings {
    val contactEmail = propertyOrNull("openFoodFacts.contactEmail")?.getString()?.trim()?.ifBlank { null }
        ?: error(
            "OFF_CONTACT_EMAIL is not set. Set it before starting the application; see README, section Configuration.",
        )
    require(CONTACT_EMAIL.matches(contactEmail)) {
        "OFF_CONTACT_EMAIL is not one e-mail address; see README, section Configuration."
    }
    return OpenFoodFactsSettings(contactEmail)
}
