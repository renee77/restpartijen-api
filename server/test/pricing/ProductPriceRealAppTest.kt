package com.restpartijen.api.pricing

import com.restpartijen.api.module
import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.product.dto.ProductResponse
import com.restpartijen.api.testsupport.testConfig
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.math.roundToLong
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * TC-07 on the real application: GET /api/v1/products/{id} shows the three price fields in euros,
 * and an expired product gives 200 with EXPIRED and no price (US-07, §5.7, B-18).
 *
 * The seed data puts one product in every discount tier, well away from a boundary, so the expected
 * discount does not depend on the exact second. module() shares one database between tests,
 * so nothing here changes data.
 */
class ProductPriceRealAppTest {

    // One product per discount tier (§9.4), with the price it must show. Amounts in cents, like Money.
    private data class Case(val name: String, val originalCents: Long, val percentage: Int, val currentCents: Long)

    private val cases = listOf(
        // FRESH: more than 48 hours, 48, 24 and 12 hours or less
        Case("Halfvolle melk 1 liter", 249, 0, 249),
        Case("Volkoren brood", 349, 10, 314),
        Case("Caesarsalade", 399, 40, 239),
        Case("Aardbeienvlaai", 500, 60, 200),
        // FROZEN: more than 7 days, 7 days and 24 hours or less
        Case("Pizza margherita", 299, 0, 299),
        Case("Diepvries boerenkool 2,5 kg", 450, 15, 382),
        Case("Kabeljauwfilet 1 kg", 1299, 40, 779),
        // AMBIENT: more than 14 days, 14 days and 7 days or less
        Case("Basmatirijst 5 kg", 899, 0, 899),
        Case("Roomboterkoekjes", 199, 15, 169),
        Case("Beschuit", 149, 40, 89),
    )

    private fun ApplicationTestBuilder.startApp() {
        environment { config = testConfig() }
        application { module() }
    }

    /** The id of a seed product by name. The first request starts the application, which seeds the database. */
    private suspend fun ApplicationTestBuilder.idOf(name: String): Long {
        client.get("/api/v1/products")
        return transaction {
            ProductsTable.selectAll().single { it[ProductsTable.name] == name }[ProductsTable.id]
        }
    }

    private suspend fun ApplicationTestBuilder.detail(name: String) = client.get("/api/v1/products/${idOf(name)}")

    // Euros in the answer, whole cents inside (B-25)
    private fun Double.toCents(): Long = (this * 100).roundToLong()

    @Test
    fun `every staffel shows the right percentage and price in euros`() = testApplication {
        startApp()

        for (case in cases) {
            val response = detail(case.name)

            assertEquals(HttpStatusCode.OK, response.status, case.name)
            val product = Json.decodeFromString<ProductResponse>(response.bodyAsText())
            assertEquals("LISTED", product.status, case.name)
            assertEquals(case.originalCents, product.originalPrice.toCents(), "original price of ${case.name}")
            assertEquals(case.percentage, product.discountPercentage, "percentage of ${case.name}")
            assertEquals(case.currentCents, product.currentPrice?.toCents(), "current price of ${case.name}")
        }
    }

    @Test
    fun `prices are in euros, not in cents`() = testApplication {
        startApp()

        val product = Json.decodeFromString<ProductResponse>(detail("Volkoren brood").bodyAsText())

        assertEquals(3.49, product.originalPrice)
        assertEquals(3.14, product.currentPrice)
    }

    @Test
    fun `a listed product past its date gives 200 with EXPIRED and no price`() = testApplication {
        startApp()

        val response = detail("Griekse yoghurt")

        assertEquals(HttpStatusCode.OK, response.status)
        val product = Json.decodeFromString<ProductResponse>(response.bodyAsText())
        assertEquals("EXPIRED", product.status)
        assertEquals(179L, product.originalPrice.toCents())
        assertNull(product.discountPercentage)
        assertNull(product.currentPrice)
    }

    @Test
    fun `a product that is already expired also gives 200 and no price`() = testApplication {
        startApp()

        val response = detail("Karnemelk 1 liter")

        assertEquals(HttpStatusCode.OK, response.status)
        val product = Json.decodeFromString<ProductResponse>(response.bodyAsText())
        assertEquals("EXPIRED", product.status)
        assertNull(product.discountPercentage)
        assertNull(product.currentPrice)
    }

    @Test
    fun `a removed product gives 404`() = testApplication {
        startApp()

        assertEquals(HttpStatusCode.NotFound, detail("Appelsap 1,5 liter").status)
    }

    @Test
    fun `an unknown product gives 404`() = testApplication {
        startApp()

        assertEquals(HttpStatusCode.NotFound, client.get("/api/v1/products/999999").status)
    }
}
