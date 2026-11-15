package nl.restpartijen.app

import nl.restpartijen.app.core.coreModule
import nl.restpartijen.app.example.exampleModule
import nl.restpartijen.app.pricing.pricingModule
import nl.restpartijen.app.product.productModule
import nl.restpartijen.app.reservation.reservationModule
import nl.restpartijen.app.security.securityModule
import org.koin.dsl.module

// The one place that knows all features (like AppNavigation.kt). Each feature registers its own
// definitions in its own module, so nobody edits this file except to add a feature (A-29).
val appModule =
    module {
        includes(
            coreModule,
            productModule,
            reservationModule,
            securityModule,
            pricingModule,
            exampleModule,
        )
    }
