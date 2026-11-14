package nl.restpartijen.app

import nl.restpartijen.app.core.network.ApiResult
import nl.restpartijen.app.example.data.ExampleRepository
import nl.restpartijen.app.example.data.FakeExampleRepository
import nl.restpartijen.app.example.ui.ExampleViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// The one place that knows all features (like AppNavigation.kt).
val appModule =
    module {
        // Walking skeleton: a fixed answer, no network.
        single<ExampleRepository> { FakeExampleRepository(ApiResult.Success(listOf("first", "second"))) }
        viewModel { ExampleViewModel(get()) }
    }
