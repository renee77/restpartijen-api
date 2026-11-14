package nl.restpartijen.app.core.network

/**
 * Outcome of a call to the API (decision 4): a failure is an ordinary result, not an exception,
 * so the compiler makes every caller handle both cases.
 */
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>

    data class Failure(val error: ApiError) : ApiResult<Nothing>
}
