package com.restpartijen.api.testsupport

import com.restpartijen.api.shared.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MoneyTest {
    @Test
    fun `euros are rounded to the nearest cent`() {
        assertEquals(29L, Money.fromEuros(0.29).cents)
    }

    @Test
    fun `3_49 euros is 349 cents`() {
        assertEquals(349L, Money.fromEuros(3.49).cents)
    }

    @Test
    fun `349 cents is 3_49 euros`() {
        assertEquals(3.49, Money.fromCents(349).toEuros())
    }

    @Test
    fun `whole euros convert without a remainder`() {
        assertEquals(500L, Money.fromEuros(5.00).cents)
    }

    @Test
    fun `negative amount is rejected`() {
        assertFailsWith<IllegalArgumentException> { Money.fromCents(-1) }
    }
}
