package nl.restpartijen.app.example

import nl.restpartijen.app.core.network.ApiResult
import nl.restpartijen.app.example.data.ExampleRepository
import nl.restpartijen.app.example.data.FakeExampleRepository
import nl.restpartijen.app.example.ui.ExampleViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Koin definitions of the example from the kick-off session; removed with the example package before hand-in. */
val exampleModule =
    module {
        // Walking skeleton: a fixed answer, no network.
        single<ExampleRepository> { FakeExampleRepository(ApiResult.Success(listOf("first", "second"))) }
        viewModel { ExampleViewModel(get()) }
    }
