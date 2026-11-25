package nl.restpartijen.app

import io.ktor.client.HttpClient
import nl.restpartijen.app.core.session.FakeSessionStore
import nl.restpartijen.app.core.session.SessionStore
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.test.verify.verify
import kotlin.test.Test
import kotlin.test.assertNotNull

// Every test uses koinModules, the same list RestpartijenApplication loads.
class AppModuleTest {
    // verify() is Koin's recommended module check (checkModules is deprecated), but still marked experimental.
    // It checks the constructor parameters of each declared type. It does not run definition lambdas, so a
    // definition bound to an interface (single<SessionStore> { … }) and androidContext() calls are not checked;
    // a concrete type with a Context parameter needs extraTypes = listOf(Context::class).
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun everyConstructorDependencyHasADefinition() {
        module { includes(koinModules) }.verify()
    }

    @Test
    fun noTypeIsDefinedTwice() {
        // Same strict mode as the app: loading fails if two modules define the same type.
        koinApplication {
            allowOverride(false)
            modules(koinModules)
        }.close()
    }

    // Configuration check: can Koin build the HttpClient from koinModules? Its behaviour is tested in HttpClientTest.
    @Test
    fun providesTheHttpClient() {
        val app = koinApplication {
            // The real SessionStore needs an Android Context, which a JVM test does not have.
            allowOverride(true)
            modules(koinModules + module { single<SessionStore> { FakeSessionStore() } })
        }

        try {
            // Runs the definition for real, which verify() does not do.
            val client = app.koin.getOrNull<HttpClient>()

            // Null when HttpClient or one of its dependencies (such as SessionStore) has no definition.
            assertNotNull(client, "Koin cannot build the HttpClient from koinModules")

            // The client does not close an engine it was given (manageEngine = false), so close both.
            client.close()
            client.engine.close()
        } finally {
            app.close()
        }
    }
}
