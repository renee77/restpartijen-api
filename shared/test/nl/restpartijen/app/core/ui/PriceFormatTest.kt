package nl.restpartijen.app.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PriceFormatTest {
    @Test
    fun `Formats cents in English with euro sign before number`() {
        assertEquals("€2.39", formatPrice(239, "en"))
    }

    @Test
    fun `Formats cents in Dutch with euro sign and space before number`() {
        assertEquals("€ 2,39", formatPrice(239, "nl"))
    }

    @Test
    fun `Formats zero cents in English as zero euros with two decimals`() {
        assertEquals("€0.00", formatPrice(0, "en"))
    }

    @Test
    fun `Formats zero cents in Dutch as zero euros with two decimals`() {
        assertEquals("€ 0,00", formatPrice(0, "nl"))
    }

    @Test
    fun `Pads single digit cents to two digits in English`() {
        assertEquals("€0.05", formatPrice(5, "en"))
    }

    @Test
    fun `Pads single digit cents to two digits in Dutch`() {
        assertEquals("€ 0,05", formatPrice(5, "nl"))
    }

    @Test
    fun `Shows two decimals for whole euros in English`() {
        assertEquals("€1.00", formatPrice(100, "en"))
    }

    @Test
    fun `Shows two decimals for whole euros in Dutch`() {
        assertEquals("€ 1,00", formatPrice(100, "nl"))
    }

    @Test
    fun `Uses the euro sign in every language`() {
        assertTrue(formatPrice(239, "en").contains("€"))
        assertTrue(formatPrice(239, "nl").contains("€"))
    }

    // Provisional: A-28 does not define grouping yet (open question 6). Confirm with Eva.
    @Test
    fun `Formats amounts above one thousand euros in English with grouping`() {
        assertEquals("€1,234.56", formatPrice(123456, "en"))
    }

    // Provisional: see above. Remove this comment before merging to main.
    @Test
    fun `Formats amounts above one thousand euros in Dutch with grouping`() {
        assertEquals("€ 1.234,56", formatPrice(123456, "nl"))
    }
}
