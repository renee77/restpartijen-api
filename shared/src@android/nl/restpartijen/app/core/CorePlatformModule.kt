package nl.restpartijen.app.core

import nl.restpartijen.app.core.session.FakeSessionStore
import nl.restpartijen.app.core.session.SessionStore
import org.koin.dsl.module

/** Android implementations of the interfaces that the core code declares (A-29, FTD §8.8). */
val corePlatformModule = module {
    // TEMPORARY: the HttpClient needs a SessionStore, and the encrypted store (A-13, GB-3) does not exist yet.
    // This one forgets the session when the app restarts. Lonneke replaces this line with her store;
    // adding a second SessionStore instead makes the app fail at start-up (strict mode, A-29).
    single<SessionStore> { FakeSessionStore() }
}
