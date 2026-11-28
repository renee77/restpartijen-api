package nl.restpartijen.app.core.session

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A [DataStore] that keeps its value in memory, for tests. It writes no file, so it behaves the same on every
 * operating system. Not safe for calls that run at the same time.
 */
internal class FakeDataStore<T>(initial: T) : DataStore<T> {
    private val current = MutableStateFlow(initial)

    override val data: Flow<T> = current.asStateFlow()

    override suspend fun updateData(transform: suspend (t: T) -> T): T {
        val updated = transform(current.value)
        current.value = updated
        return updated
    }
}
