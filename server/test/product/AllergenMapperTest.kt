package com.restpartijen.api.product

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.restpartijen.api.product.service.mapAllergenTags
import com.restpartijen.api.shared.Allergen
import org.slf4j.LoggerFactory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit test: mapAllergenTags against the tag ids of the Open Food Facts allergen taxonomy (§9.8, §10.3).
 * The tags come from https://world.openfoodfacts.org/data/taxonomies/allergens.json, read on 22 Oct 2026.
 */
class AllergenMapperTest {

    @Test
    fun `mapAllergenTags maps each EU allergen tag to its own Allergen`() {
        // One assertion per tag, so a failure names the tag; three names differ from ours (gluten, sesame, sulphites).
        for ((tag, allergen) in euTags) {
            assertEquals(setOf(allergen), mapAllergenTags(listOf(tag)), tag)
        }
    }

    @Test
    fun `the table covers all fourteen EU allergens`() {
        assertEquals(Allergen.entries.toSet(), euTags.values.toSet())
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
        val events = captureLogEvents()

        // Act
        mapAllergenTags(listOf("en:kiwi"))

        // Assert
        val warning = events.about("en:kiwi").single()
        assertEquals(Level.WARN, warning.level)
    }

    @Test
    fun `mapAllergenTags skips en none without a warning`() {
        // Arrange
        // en:none means someone stated "no allergens"; it is a known tag, so it is no reason to warn.
        val events = captureLogEvents()

        // Act
        val allergens = mapAllergenTags(listOf("en:none"))

        // Assert
        assertEquals(emptySet(), allergens)
        assertEquals(emptyList(), events.about("en:none"))
    }

    @Test
    fun `mapAllergenTags logs an unknown tag on one line and at most 64 characters of it`() {
        // Arrange
        // The tag comes from Open Food Facts; a line break or another control character in it could forge
        // or garble a log line (CWE-117). CR, LF, ESC, NEL (U+0085) and LINE SEPARATOR (U+2028) all appear.
        val events = captureLogEvents()
        val tag = "en:x\r\nFAKE\u001B[31m\u0085\u2028" + "y".repeat(100)

        // Act
        mapAllergenTags(listOf(tag))

        // Assert
        val message = events.about("en:x").single().formattedMessage
        assertFalse(Regex("[\\p{Cc}\\p{Cf}\\p{Zl}\\p{Zp}]").containsMatchIn(message), message)
        // 17 characters before the y's, so 47 of them fit in the 64.
        assertTrue(message.endsWith("en:x__FAKE_[31m__" + "y".repeat(47)), message)
    }

    @Test
    fun `mapAllergenTags logs one line per call with the number of unknown tags and at most 5 of them`() {
        // Arrange
        // One answer with many odd tags must not become one log line per tag.
        val events = captureLogEvents()
        val tags = (1..12).map { "en:odd-$it" }

        // Act
        mapAllergenTags(tags)

        // Assert
        val message = events.about("en:odd-1").single().formattedMessage
        assertTrue(message.contains("Skipped 12 unknown"), message)
        assertTrue(message.contains("en:odd-5"), message)
        assertFalse(message.contains("en:odd-6"), message)
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

    /** Collects what the mapper logs during one test; detached afterwards so tests do not see each other's logs. */
    private fun captureLogEvents(): ListAppender<ILoggingEvent> {
        val logger = LoggerFactory.getLogger(MAPPER_LOGGER) as Logger
        val appender = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(appender)
        attached += appender
        return appender
    }

    /** Only the events about [tag], so a log line from a test running at the same time cannot interfere. */
    private fun ListAppender<ILoggingEvent>.about(tag: String) = list.filter { it.formattedMessage.contains(tag) }

    /** The appenders this test attached, so [detachAppenders] can remove them. */
    private val attached = mutableListOf<ListAppender<ILoggingEvent>>()

    /** Removes the appenders of this test from the logger after each test. */
    @AfterTest
    fun detachAppenders() {
        val logger = LoggerFactory.getLogger(MAPPER_LOGGER) as Logger
        attached.forEach { logger.detachAppender(it) }
    }

    private companion object {
        /** The logger name of AllergenMapper; it logs under the file name, because it is not a class. */
        const val MAPPER_LOGGER = "com.restpartijen.api.product.service.AllergenMapper"

        /** The fourteen EU tags of the taxonomy and the Allergen each one must become (§9.8). */
        val euTags = mapOf(
            "en:gluten" to Allergen.CEREALS_CONTAINING_GLUTEN,
            "en:crustaceans" to Allergen.CRUSTACEANS,
            "en:eggs" to Allergen.EGGS,
            "en:fish" to Allergen.FISH,
            "en:peanuts" to Allergen.PEANUTS,
            "en:soybeans" to Allergen.SOYBEANS,
            "en:milk" to Allergen.MILK,
            "en:nuts" to Allergen.NUTS,
            "en:celery" to Allergen.CELERY,
            "en:mustard" to Allergen.MUSTARD,
            "en:sesame-seeds" to Allergen.SESAME,
            "en:sulphur-dioxide-and-sulphites" to Allergen.SULPHITES,
            "en:lupin" to Allergen.LUPIN,
            "en:molluscs" to Allergen.MOLLUSCS,
        )
    }
}
