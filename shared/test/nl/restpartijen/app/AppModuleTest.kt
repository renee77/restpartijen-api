package nl.restpartijen.app

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify
import kotlin.test.Test

class AppModuleTest {
    // verify() is Koin's recommended module check (checkModules is deprecated), but still marked experimental.
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun koinCanBuildEveryDefinition() {
        // Same modules as RestpartijenApplication loads, so a missing Android implementation fails here too.
        module { includes(appModule, platformModule) }.verify()
    }
}
