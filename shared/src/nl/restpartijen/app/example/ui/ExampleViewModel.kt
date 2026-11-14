package nl.restpartijen.app.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nl.restpartijen.app.core.network.ApiResult
import nl.restpartijen.app.example.data.ExampleRepository

/** Loads the example list once on creation; [load] tries again, for example after an error. */
class ExampleViewModel(
    private val repository: ExampleRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ExampleUiState>(ExampleUiState.Loading)
    val uiState: StateFlow<ExampleUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ExampleUiState.Loading
            _uiState.value =
                when (val result = repository.loadItems()) {
                    is ApiResult.Success -> ExampleUiState.Success(result.data)
                    is ApiResult.Failure -> ExampleUiState.Error
                }
        }
    }
}
