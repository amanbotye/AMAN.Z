package ye.aman.admin.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ye.aman.admin.core.result.AppResult
import ye.aman.admin.domain.model.AdminDashboardSummary
import ye.aman.admin.domain.usecase.GetAdminDashboardSummaryUseCase

data class AdminDashboardUiState(
    val summary: AdminDashboardSummary = AdminDashboardSummary(0, 0, 0, 0, 0.0, 0),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AdminDashboardViewModel(
    private val getDashboardSummaryUseCase: GetAdminDashboardSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
    }

    fun loadSummary() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val res = getDashboardSummaryUseCase()) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(summary = res.data, isLoading = false)
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = res.error.message
                    )
                }
                is AppResult.Loading -> Unit
            }
        }
    }
}
