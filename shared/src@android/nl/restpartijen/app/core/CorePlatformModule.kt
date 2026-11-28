package nl.restpartijen.app.core

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import nl.restpartijen.app.core.session.SessionStore
import nl.restpartijen.app.core.session.encryptedSessionStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** Android implementations of the interfaces that the core code declares (A-29, FTD §8.8). */
val corePlatformModule = module {
    // OkHttp is the Android engine, so it lives here and not in coreModule (A-29). Tests use MockEngine instead.
    single<HttpClientEngine> { OkHttp.create() }

    // The encrypted session store (A-13, GB-3). This is the only definition of SessionStore: a second one makes
    // the app fail at start-up (strict mode, A-29). Tests use FakeSessionStore, which lives in test/.
    single<SessionStore> { encryptedSessionStore(androidContext()) }
}
