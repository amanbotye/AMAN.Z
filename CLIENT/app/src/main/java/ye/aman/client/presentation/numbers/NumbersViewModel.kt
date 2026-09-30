package ye.aman.client.presentation.numbers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ye.aman.client.core.result.AppResult
import ye.aman.client.domain.model.CustomerNumber
import ye.aman.client.domain.usecase.AddCustomerNumberUseCase
import ye.aman.client.domain.usecase.LoadCustomerNumbersUseCase

data class NumbersUiState(
    val isLoading: Boolean = false,
    val selectedTab: Int = 0, // 0 = Protected, 1 = Not Protected
    val searchQuery: String = "",
    val numbers: List<CustomerNumber> = emptyList(),
    val errorMessage: String? = null,
    val addSuccess: Boolean = false
)

class NumbersViewModel(
    private val loadNumbersUseCase: LoadCustomerNumbersUseCase,
    private val addNumberUseCase: AddCustomerNumberUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NumbersUiState())
    val uiState: StateFlow<NumbersUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadNumbersUseCase().collect { list ->
                _uiState.value = _uiState.value.copy(numbers = list)
            }
        }
    }

    fun selectTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun updateSearch(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun addNumber(phone: String, label: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val res = addNumberUseCase(phone, label)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, addSuccess = true)
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = res.error.message)
                }
            }
        }
    }

    fun clearAddSuccess() {
        _uiState.value = _uiState.value.copy(addSuccess = false, errorMessage = null)
    }
}
