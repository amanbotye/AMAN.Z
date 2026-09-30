package ye.aman.client.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ye.aman.client.domain.model.CustomerNumber
import ye.aman.client.domain.model.Protection
import ye.aman.client.domain.usecase.LoadCustomerNumbersUseCase
import ye.aman.client.domain.usecase.LoadProtectionsUseCase
import ye.aman.client.domain.usecase.SyncAllDataUseCase

data class HomeUiState(
    val isLoading: Boolean = false,
    val totalNumbers: Int = 0,
    val protectedNumbers: Int = 0,
    val notProtectedNumbers: Int = 0,
    val activeProtections: List<Protection> = emptyList(),
    val numbersList: List<CustomerNumber> = emptyList()
)

class HomeViewModel(
    private val loadNumbersUseCase: LoadCustomerNumbersUseCase,
    private val loadProtectionsUseCase: LoadProtectionsUseCase,
    private val syncAllDataUseCase: SyncAllDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeData()
        refresh()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                loadNumbersUseCase(),
                loadProtectionsUseCase()
            ) { numbers, protections ->
                val protectedCount = numbers.count { it.isProtected }
                HomeUiState(
                    isLoading = false,
                    totalNumbers = numbers.size,
                    protectedNumbers = protectedCount,
                    notProtectedNumbers = numbers.size - protectedCount,
                    activeProtections = protections,
                    numbersList = numbers
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            syncAllDataUseCase()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}
