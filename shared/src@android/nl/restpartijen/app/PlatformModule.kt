package nl.restpartijen.app

import nl.restpartijen.app.core.corePlatformModule
import nl.restpartijen.app.product.productPlatformModule
import nl.restpartijen.app.reservation.reservationPlatformModule
import nl.restpartijen.app.security.securityPlatformModule
import org.koin.dsl.module

// Lives in src@android because src cannot see Android code. Not expect/actual: the build accepts it,
// but IntelliJ marks it as an error in a single-platform module (docs/tooling-issues/).

/** All Android implementations of interfaces from the shared code; loaded next to [appModule] (A-29). */
val platformModule =
    module {
        includes(
            corePlatformModule,
            productPlatformModule,
            reservationPlatformModule,
            securityPlatformModule,
        )
    }

/**
 * Every module the app loads at start-up: the shared code and its Android implementations.
 * RestpartijenApplication and AppModuleTest both use this list, so the test checks exactly what the app loads.
 */
val koinModules = listOf(appModule, platformModule)
