package nl.restpartijen.app.security.ui

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import nl.restpartijen.app.core.model.Role
import nl.restpartijen.app.core.model.Session
import nl.restpartijen.app.core.session.FakeSessionStore
import nl.restpartijen.app.core.session.SessionStore
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
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
    fun startsWaitingWithNothingSaved() = runTest {
        val store = FakeSessionStore()
        val viewModel = LoginViewModel(store)

        assertEquals(LoginUiState.Waiting, viewModel.uiState.value)
        assertNull(store.session.first())
    }

    @Test
    fun savesTheCollectorSession() = runTest {
        val store = FakeSessionStore()
        val viewModel = LoginViewModel(store)

        viewModel.loginAsCollector()

        assertEquals(Session("test-token", Role.COLLECTOR, supplierId = null), store.session.first())
        assertEquals(LoginUiState.Done, viewModel.uiState.value)
    }

    @Test
    fun savesTheSupplierSession() = runTest {
        val store = FakeSessionStore()
        val viewModel = LoginViewModel(store)

        viewModel.loginAsSupplier()

        assertEquals(Session("test-token", Role.SUPPLIER, supplierId = 1), store.session.first())
        assertEquals(LoginUiState.Done, viewModel.uiState.value)
    }

    @Test
    fun showsAnErrorWhenSavingFails() = runTest {
        val viewModel = LoginViewModel(FailingSessionStore())

        viewModel.loginAsCollector()

        assertEquals(LoginUiState.Error, viewModel.uiState.value)
    }

    @Test
    fun goesBackToWaitingAfterRetry() = runTest {
        val viewModel = LoginViewModel(FailingSessionStore())
        viewModel.loginAsCollector()

        viewModel.retry()

        assertEquals(LoginUiState.Waiting, viewModel.uiState.value)
    }

    @Test
    fun ignoresASecondChoiceWhileSaving() = runTest {
        val store = GatedSessionStore()
        val viewModel = LoginViewModel(store)

        viewModel.loginAsCollector()
        assertEquals(LoginUiState.Saving, viewModel.uiState.value)

        // The second tap arrives while the first save is still running.
        viewModel.loginAsSupplier()
        store.release()

        assertEquals(listOf(Session("test-token", Role.COLLECTOR, null)), store.saved)
        assertEquals(LoginUiState.Done, viewModel.uiState.value)
    }
}

/** A store whose save always fails, like a DataStore that cannot write to disk. */
private class FailingSessionStore : SessionStore {
    override val session: Flow<Session?> = flowOf(null)

    override suspend fun save(session: Session): Unit = throw IOException("The disk is full.")

    override suspend fun clear() = Unit
}

/** A store whose save waits until the test calls [release], so a test can act while saving is busy. */
private class GatedSessionStore : SessionStore {
    private val gate = CompletableDeferred<Unit>()

    val saved = mutableListOf<Session>()

    override val session: Flow<Session?> = flowOf(null)

    override suspend fun save(session: Session) {
        gate.await()
        saved += session
    }

    override suspend fun clear() = Unit

    fun release() {
        gate.complete(Unit)
    }
}
