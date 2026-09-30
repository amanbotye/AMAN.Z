package ye.aman.admin.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ye.aman.admin.core.result.AppResult
import ye.aman.admin.domain.model.*
import ye.aman.admin.domain.usecase.SettingsUseCases

data class AdminSettingsUiState(
    val selectedTab: Int = 0, // 0: Providers, 1: Packages, 2: PaymentMethods, 3: AuditLogs, 4: SystemSettings
    val providers: List<TelecomProvider> = emptyList(),
    val packages: List<Package> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val auditLogs: List<AuditLog> = emptyList(),
    val systemSettings: List<SystemSetting> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AdminSettingsViewModel(
    private val useCases: SettingsUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminSettingsUiState())
    val uiState: StateFlow<AdminSettingsUiState> = _uiState.asStateFlow()

    init {
        observeData()
        loadData()
    }

    private fun observeData() {
        viewModelScope.launch {
            useCases.getProviders().collect { list ->
                _uiState.value = _uiState.value.copy(providers = list)
            }
        }
        viewModelScope.launch {
            useCases.getPackages().collect { list ->
                _uiState.value = _uiState.value.copy(packages = list)
            }
        }
        viewModelScope.launch {
            useCases.getPaymentMethods().collect { list ->
                _uiState.value = _uiState.value.copy(paymentMethods = list)
            }
        }
    }

    fun selectTab(idx: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = idx)
        if (idx == 3) loadAuditLogs()
        if (idx == 4) loadSystemSettings()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            useCases.refreshProviders()
            useCases.refreshPackages()
            useCases.refreshPaymentMethods()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun loadAuditLogs() {
        viewModelScope.launch {
            when (val res = useCases.getAuditLogs()) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(auditLogs = res.data)
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = res.error.message)
                is AppResult.Loading -> Unit
            }
        }
    }

    fun loadSystemSettings() {
        viewModelScope.launch {
            when (val res = useCases.getSystemSettings()) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(systemSettings = res.data)
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = res.error.message)
                is AppResult.Loading -> Unit
            }
        }
    }
}
