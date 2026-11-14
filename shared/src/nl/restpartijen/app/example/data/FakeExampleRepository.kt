package nl.restpartijen.app.example.data

import nl.restpartijen.app.core.network.ApiResult

/** Answers with the [result] it was created with, so the caller decides whether loading succeeds or fails. */
class FakeExampleRepository(
    private val result: ApiResult<List<String>>,
) : ExampleRepository {
    override suspend fun loadItems(): ApiResult<List<String>> = result
}
