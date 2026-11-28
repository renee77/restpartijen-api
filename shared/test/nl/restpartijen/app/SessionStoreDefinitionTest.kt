package nl.restpartijen.app

import nl.restpartijen.app.core.session.FakeSessionStore
import nl.restpartijen.app.core.session.SessionStore
import org.koin.core.annotation.KoinInternalApi
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SessionStoreDefinitionTest {
    // Koin has no public way to list its definitions, so this test uses the internal registry. It only reads.
    @OptIn(KoinInternalApi::class)
    @Test
    fun koinDefinesExactlyOneSessionStoreAndItIsNotTheFake() {
        val app = koinApplication { modules(koinModules) }

        try {
            // Loading the modules does not run a definition, so no Android Context is needed.
            val definitions = app.koin.instanceRegistry.instances.values.map { it.beanDefinition }.distinct()
            val types = definitions.map { listOf(it.primaryType) + it.secondaryTypes }

            assertEquals(1, types.count { SessionStore::class in it }, "Koin must define exactly one SessionStore")
            assertTrue(types.none { FakeSessionStore::class in it }, "FakeSessionStore must not be defined in Koin")
        } finally {
            app.close()
        }
    }
}
