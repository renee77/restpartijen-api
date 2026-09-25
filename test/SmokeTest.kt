// Same package as the production code, mirrored under test/
package com.restpartijen.api

import kotlin.test.Test
import kotlin.test.assertTrue

// Smoke test: proves that the test setup of the Kotlin Toolchain finds and runs tests in test/.
class SmokeTest {

    @Test
    fun testSetupRuns() {
        // Temporarily change to assertTrue(false) once to see the test fail, then change it back
        assertTrue(true)
    }
}