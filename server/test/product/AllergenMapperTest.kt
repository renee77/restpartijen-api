package com.restpartijen.api.product

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.restpartijen.api.product.service.mapAllergenTags
import com.restpartijen.api.shared.Allergen
import org.slf4j.LoggerFactory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit test: mapAllergenTags against the tag ids of the Open Food Facts allergen taxonomy (§9.8, §10.3).
 * The tags come from https://world.openfoodfacts.org/data/taxonomies/allergens.json, read on 22 Oct 2026.
 */
class AllergenMapperTest {

    @Test
    fun `mapAllergenTags maps every EU allergen tag to its Allergen`() {
        // Arrange
        val tags = listOf(
            "en:gluten", "en:crustaceans", "en:eggs", "en:fish", "en:peanuts", "en:soybeans", "en:milk",
            "en:nuts", "en:celery", "en:mustard", "en:sesame-seeds", "en:sulphur-dioxide-and-sulphites",
            "en:lupin", "en:molluscs",
        )

        // Act
        val allergens = mapAllergenTags(tags)

        // Assert
        assertEquals(Allergen.entries.toSet(), allergens)
    }

    @Test
    fun `mapAllergenTags maps the tags whose name differs from the Allergen`() {
        // Arrange
        // The three tags where removing "en:" and using capitals is not enough.
        val tags = listOf("en:gluten", "en:sesame-seeds", "en:sulphur-dioxide-and-sulphites")

        // Act
        val allergens = mapAllergenTags(tags)

        // Assert
        assertEquals(setOf(Allergen.CEREALS_CONTAINING_GLUTEN, Allergen.SESAME, Allergen.SULPHITES), allergens)
    }

    @Test
    fun `mapAllergenTags skips tags that are not one of the fourteen EU allergens`() {
        // Arrange
        // en:kiwi is in the taxonomy but not in Annex II; fr:noisettes was never matched to the taxonomy.
        val tags = listOf("en:kiwi", "en:eggs", "en:none", "fr:noisettes")

        // Act
        val allergens = mapAllergenTags(tags)

        // Assert
        assertEquals(setOf(Allergen.EGGS), allergens)
    }

    @Test
    fun `mapAllergenTags warns about a tag it does not know`() {
        // Arrange
        val warnings = captureWarnings()

        // Act
        mapAllergenTags(listOf("en:kiwi"))

        // Assert
        assertEquals(listOf("Skipped unknown allergen tag from Open Food Facts: en:kiwi"), warnings.messages())
    }

    @Test
    fun `mapAllergenTags skips en none without a warning`() {
        // Arrange
        // en:none means someone stated "no allergens"; it is a known tag, so it is no reason to warn.
        val warnings = captureWarnings()

        // Act
        val allergens = mapAllergenTags(listOf("en:none"))

        // Assert
        assertEquals(emptySet(), allergens)
        assertEquals(emptyList(), warnings.messages())
    }

    @Test
    fun `mapAllergenTags returns each allergen once`() {
        // Arrange
        val tags = listOf("en:milk", "en:milk")

        // Act
        val allergens = mapAllergenTags(tags)

        // Assert
        assertEquals(setOf(Allergen.MILK), allergens)
    }

    @Test
    fun `mapAllergenTags returns no allergens for no tags`() {
        // Act
        val allergens = mapAllergenTags(emptyList())

        // Assert
        assertEquals(emptySet(), allergens)
    }

    // Collects what the mapper logs during one test; detached afterwards so tests do not see each other's logs.
    private fun captureWarnings(): ListAppender<ILoggingEvent> {
        val logger = LoggerFactory.getLogger(MAPPER_LOGGER) as Logger
        val appender = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(appender)
        attached += appender
        return appender
    }

    private fun ListAppender<ILoggingEvent>.messages() = list.map { it.formattedMessage }

    private val attached = mutableListOf<ListAppender<ILoggingEvent>>()

    @AfterTest
    fun detachAppenders() {
        val logger = LoggerFactory.getLogger(MAPPER_LOGGER) as Logger
        attached.forEach { logger.detachAppender(it) }
    }

    private companion object {
        const val MAPPER_LOGGER = "com.restpartijen.api.product.service.AllergenMapper"
    }
}
