package com.restpartijen.api.pricing

import com.restpartijen.api.pricing.service.ExpiryScheduler
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.plugins.di.dependencies
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

// Interval between two maintenance runs; a late EXPIRED is harmless because search and reserve check the time
// themselves.
private val MAINTENANCE_INTERVAL = 5.minutes

/**
 * Starts the periodic maintenance run (US-08, A8). The loop lives in the
 * application's scope, so it stops when the application stops.
 */
fun Application.configureMaintenanceLoop() {
    val scheduler: ExpiryScheduler by dependencies

    launch {
        while (isActive) {
            delay(MAINTENANCE_INTERVAL)
            try {
                scheduler.runMaintenance()
            } catch (e: CancellationException) {
                throw e // never swallow this one, or the loop cannot stop on shutdown
            } catch (e: Exception) {
                log.error("Maintenance run failed", e)
            }
        }
    }
}
