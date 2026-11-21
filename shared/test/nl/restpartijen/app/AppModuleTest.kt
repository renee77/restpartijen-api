package nl.restpartijen.app

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
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
        // HttpClient's constructor takes an engine, but createHttpClient passes it in the lambda, not via Koin.
        module { includes(koinModules) }.verify(extraTypes = listOf(HttpClientEngine::class))
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
        val app = koinApplication { modules(koinModules) }

        // Runs the definition for real, which verify() does not do.
        val client = app.koin.getOrNull<HttpClient>()

        assertNotNull(client, "coreModule has no HttpClient")
        client.close()
        app.close()
    }
}
