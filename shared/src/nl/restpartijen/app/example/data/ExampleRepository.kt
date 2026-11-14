package nl.restpartijen.app.example.data

import nl.restpartijen.app.core.network.ApiResult

/** Supplies the example list. The ViewModel only knows this interface, so tests and later the real API can swap in. */
interface ExampleRepository {
    suspend fun loadItems(): ApiResult<List<String>>
}
