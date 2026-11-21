package nl.restpartijen.app.core

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import nl.restpartijen.app.core.session.FakeSessionStore
import nl.restpartijen.app.core.session.SessionStore
import org.koin.dsl.module

/** Android implementations of the interfaces that the core code declares (A-29, FTD §8.8). */
val corePlatformModule = module {
    // OkHttp is the Android engine, so it lives here and not in coreModule (A-29). Tests use MockEngine instead.
    single<HttpClientEngine> { OkHttp.create() }

    // TEMPORARY: the HttpClient needs a SessionStore, and the encrypted store (A-13, #113) does not exist yet.
    // This one forgets the session when the app restarts. The encrypted store replaces this line;
    // adding a second SessionStore instead makes the app fail at start-up (strict mode, A-29).
    single<SessionStore> { FakeSessionStore() }
}
