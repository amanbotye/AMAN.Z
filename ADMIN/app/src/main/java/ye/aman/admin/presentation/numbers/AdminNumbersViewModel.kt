package ye.aman.admin.presentation.numbers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ye.aman.admin.core.result.AppResult
import ye.aman.admin.domain.model.AdminPhoneNumber
import ye.aman.admin.domain.usecase.GetAdminNumbersUseCase

data class AdminNumbersUiState(
    val numbers: List<AdminPhoneNumber> = emptyList(),
    val filteredNumbers: List<AdminPhoneNumber> = emptyList(),
    val searchQuery: String = "",
    val filterType: String = "all", // all, protected, unprotected
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AdminNumbersViewModel(
    private val getNumbersUseCase: GetAdminNumbersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminNumbersUiState())
    val uiState: StateFlow<AdminNumbersUiState> = _uiState.asStateFlow()

    init {
        observeNumbers()
        refresh()
    }

    private fun observeNumbers() {
        viewModelScope.launch {
            getNumbersUseCase().collect { list ->
                _uiState.value = _uiState.value.copy(
                    numbers = list,
                    filteredNumbers = applyFilter(list, _uiState.value.searchQuery, _uiState.value.filterType)
                )
            }
        }
    }

    fun onSearchChange(q: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = q,
            filteredNumbers = applyFilter(_uiState.value.numbers, q, _uiState.value.filterType)
        )
    }

    fun onFilterChange(type: String) {
        _uiState.value = _uiState.value.copy(
            filterType = type,
            filteredNumbers = applyFilter(_uiState.value.numbers, _uiState.value.searchQuery, type)
        )
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val res = getNumbersUseCase.refresh()
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (res is AppResult.Error) {
                _uiState.value = _uiState.value.copy(errorMessage = res.error.message)
            }
        }
    }

    private fun applyFilter(list: List<AdminPhoneNumber>, q: String, filter: String): List<AdminPhoneNumber> {
        return list.filter { item ->
            val matchSearch = q.isBlank() || item.phoneNumber.contains(q) || item.providerName.contains(q)
            val matchFilter = when (filter) {
                "protected" -> item.isProtected
                "unprotected" -> !item.isProtected
                else -> true
            }
            matchSearch && matchFilter
        }
    }
}
