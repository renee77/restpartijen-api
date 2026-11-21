package nl.restpartijen.app.core

import io.ktor.client.engine.okhttp.OkHttp
import nl.restpartijen.app.core.network.createHttpClient
import org.koin.dsl.module

/** Koin definitions of the shared core that every feature uses; Android parts are in corePlatformModule (A-29). */
val coreModule = module {
    // One client for the whole app, so every repository uses the same settings (FTD §8.6).
    single { createHttpClient(OkHttp.create()) }
}
