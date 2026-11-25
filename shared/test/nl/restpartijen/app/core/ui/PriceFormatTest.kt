package nl.restpartijen.app.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PriceFormatTest {
    @Test
    fun formatsCentsInEnglishWithEuroSignBeforeNumber() {
        assertEquals("€2.39", formatPrice(239, "en"))
    }

    @Test
    fun formatsCentsInDutchWithEuroSignAndSpaceBeforeNumber() {
        assertEquals("€ 2,39", formatPrice(239, "nl"))
    }

    @Test
    fun formatsZeroCentsInEnglishAsZeroEurosWithTwoDecimals() {
        assertEquals("€0.00", formatPrice(0, "en"))
    }

    @Test
    fun formatsZeroCentsInDutchAsZeroEurosWithTwoDecimals() {
        assertEquals("€ 0,00", formatPrice(0, "nl"))
    }

    @Test
    fun padsSingleDigitCentsToTwoDigitsInEnglish() {
        assertEquals("€0.05", formatPrice(5, "en"))
    }

    @Test
    fun padsSingleDigitCentsToTwoDigitsInDutch() {
        assertEquals("€ 0,05", formatPrice(5, "nl"))
    }

    @Test
    fun showsTwoDecimalsForWholeEurosInEnglish() {
        assertEquals("€1.00", formatPrice(100, "en"))
    }

    @Test
    fun showsTwoDecimalsForWholeEurosInDutch() {
        assertEquals("€ 1,00", formatPrice(100, "nl"))
    }

    @Test
    fun usesTheEuroSignInEveryLanguage() {
        assertTrue(formatPrice(239, "en").contains("€"))
        assertTrue(formatPrice(239, "nl").contains("€"))
    }

    @Test
    fun usesEnglishNotationForEnglishWithRegionUs() {
        assertEquals("€2.39", formatPrice(239, "en-US"))
    }

    @Test
    fun usesEnglishNotationForEnglishWithRegionGb() {
        assertEquals("€2.39", formatPrice(239, "en-GB"))
    }

    @Test
    fun usesDutchNotationForDutchWithRegionNl() {
        assertEquals("€ 2,39", formatPrice(239, "nl-NL"))
    }
}
