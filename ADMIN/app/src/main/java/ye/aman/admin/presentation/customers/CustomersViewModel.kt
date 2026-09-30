package ye.aman.admin.presentation.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ye.aman.admin.core.result.AppResult
import ye.aman.admin.domain.model.CustomerRecord
import ye.aman.admin.domain.usecase.GetCustomersUseCase
import ye.aman.admin.domain.usecase.UpdateCustomerStatusUseCase

data class CustomersUiState(
    val customers: List<CustomerRecord> = emptyList(),
    val filteredCustomers: List<CustomerRecord> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedCustomer: CustomerRecord? = null
)

class CustomersViewModel(
    private val getCustomersUseCase: GetCustomersUseCase,
    private val updateCustomerStatusUseCase: UpdateCustomerStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomersUiState())
    val uiState: StateFlow<CustomersUiState> = _uiState.asStateFlow()

    init {
        observeCustomers()
        refresh()
    }

    private fun observeCustomers() {
        viewModelScope.launch {
            getCustomersUseCase().collect { list ->
                _uiState.value = _uiState.value.copy(
                    customers = list,
                    filteredCustomers = filterList(list, _uiState.value.searchQuery)
                )
            }
        }
    }

    fun onSearchChange(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredCustomers = filterList(_uiState.value.customers, query)
        )
    }

    fun selectCustomer(customer: CustomerRecord?) {
        _uiState.value = _uiState.value.copy(selectedCustomer = customer)
    }

    fun updateStatus(customerId: String, newStatus: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val res = updateCustomerStatusUseCase(customerId, newStatus)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, selectedCustomer = null)
                    refresh()
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = res.error.message)
                }
                is AppResult.Loading -> Unit
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val res = getCustomersUseCase.refresh()
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (res is AppResult.Error) {
                _uiState.value = _uiState.value.copy(errorMessage = res.error.message)
            }
        }
    }

    private fun filterList(list: List<CustomerRecord>, q: String): List<CustomerRecord> {
        if (q.isBlank()) return list
        return list.filter {
            it.fullName.contains(q, ignoreCase = true) ||
            it.phone.contains(q) ||
            (it.email?.contains(q, ignoreCase = true) == true)
        }
    }
}
