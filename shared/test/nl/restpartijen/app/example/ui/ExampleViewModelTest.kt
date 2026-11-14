package nl.restpartijen.app.example.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import nl.restpartijen.app.core.network.ApiError
import nl.restpartijen.app.core.network.ApiResult
import nl.restpartijen.app.example.data.FakeExampleRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ExampleViewModelTest {
    @BeforeTest
    fun setMainDispatcher() {
        // Unconfined: a launched coroutine runs straight away, so the state is final after each call.
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun showsTheItemsWhenLoadingSucceeds() =
        runTest {
            val viewModel = ExampleViewModel(FakeExampleRepository(ApiResult.Success(listOf("first", "second"))))

            assertEquals(ExampleUiState.Success(listOf("first", "second")), viewModel.uiState.value)
        }

    @Test
    fun showsAnErrorWhenLoadingFails() =
        runTest {
            val viewModel = ExampleViewModel(FakeExampleRepository(ApiResult.Failure(ApiError.Network)))

            assertEquals(ExampleUiState.Error, viewModel.uiState.value)
        }
}
