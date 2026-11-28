package nl.restpartijen.app.security.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import nl.restpartijen.app.core.session.SessionStore

class LoginViewModel(private val sessionStore: SessionStore) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Waiting)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun loginAsCollector() = Unit

    fun loginAsSupplier() = Unit

    fun retry() = Unit
}
