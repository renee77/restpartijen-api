package nl.restpartijen.app

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.test.verify.verify
import kotlin.test.Test

// Both tests use koinModules, the same list RestpartijenApplication loads.
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
}
