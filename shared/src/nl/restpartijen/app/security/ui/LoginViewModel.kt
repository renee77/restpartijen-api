package nl.restpartijen.app.security.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import nl.restpartijen.app.core.model.Role
import nl.restpartijen.app.core.model.Session
import nl.restpartijen.app.core.session.SessionStore
import java.io.IOException
import java.security.GeneralSecurityException

// TEMPORARY: a fixed test session per role.
private val COLLECTOR_SESSION = Session(token = "test-token", role = Role.COLLECTOR, supplierId = null)

// supplierId 1 is the first supplier in the seed data.
private val SUPPLIER_SESSION = Session(token = "test-token", role = Role.SUPPLIER, supplierId = 1)

/**
 * TEMPORARY test login: saves a fixed session for the chosen role, so the rest of the app can be tried out.
 * Replaced by the real login screen.
 */
class LoginViewModel(private val sessionStore: SessionStore) : ViewModel() {
    val uiState: StateFlow<LoginUiState>
        field = MutableStateFlow<LoginUiState>(LoginUiState.Waiting)

    fun loginAsCollector() = login(COLLECTOR_SESSION)

    fun loginAsSupplier() = login(SUPPLIER_SESSION)

    /** Goes back to Waiting after an error. Only from an error: it must not interrupt a save. */
    fun retry() {
        uiState.compareAndSet(LoginUiState.Error, LoginUiState.Waiting)
    }

    private fun login(session: Session) {
        // Check and set in one step, before the coroutine starts: a second tap that comes in while the first
        // save is busy (or after it is done) finds a state other than Waiting and does nothing.
        if (!uiState.compareAndSet(LoginUiState.Waiting, LoginUiState.Saving)) return

        viewModelScope.launch {
            try {
                sessionStore.save(session)
                uiState.value = LoginUiState.Done
            } catch (_: IOException) {
                // The DataStore could not write the file.
                uiState.value = LoginUiState.Error
            } catch (_: GeneralSecurityException) {
                // Tink could not encrypt the session.
                uiState.value = LoginUiState.Error
            }
            // CancellationException is deliberately not caught: it is how a coroutine is stopped, and it is
            // neither an IOException nor a GeneralSecurityException, so it passes through.
        }
    }
}
