package ye.aman.client.presentation.protection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ye.aman.client.core.common.Constants
import ye.aman.client.core.result.AppResult
import ye.aman.client.domain.model.Package
import ye.aman.client.domain.model.PaymentMethod
import ye.aman.client.domain.repository.PackageRepository
import ye.aman.client.domain.repository.PaymentMethodRepository
import ye.aman.client.domain.usecase.CreateProtectionRequestUseCase
import ye.aman.client.domain.usecase.CreateRenewalRequestUseCase

data class ProtectionUiState(
    val isLoading: Boolean = false,
    val defaultPackage: Package? = null,
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val selectedPaymentMethod: PaymentMethod? = null,
    val paymentReference: String = "",
    val errorMessage: String? = null,
    val isSubmitted: Boolean = false
)

class ProtectionViewModel(
    private val packageRepository: PackageRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val createProtectionRequestUseCase: CreateProtectionRequestUseCase,
    private val createRenewalRequestUseCase: CreateRenewalRequestUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProtectionUiState())
    val uiState: StateFlow<ProtectionUiState> = _uiState.asStateFlow()

    init {
        loadDefaults()
    }

    private fun loadDefaults() {
        viewModelScope.launch {
            when (val res = packageRepository.getV1DefaultPackage()) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(defaultPackage = res.data)
                is AppResult.Error -> {}
            }
            paymentMethodRepository.refreshPaymentMethods()
            paymentMethodRepository.getActivePaymentMethods().collect { methods ->
                _uiState.value = _uiState.value.copy(
                    paymentMethods = methods,
                    selectedPaymentMethod = _uiState.value.selectedPaymentMethod ?: methods.firstOrNull()
                )
            }
        }
    }

    fun selectPaymentMethod(pm: PaymentMethod) {
        _uiState.value = _uiState.value.copy(selectedPaymentMethod = pm)
    }

    fun updateReference(ref: String) {
        _uiState.value = _uiState.value.copy(paymentReference = ref)
    }

    fun submitNewProtection(customerNumberId: String) {
        val pkg = _uiState.value.defaultPackage ?: return
        val pm = _uiState.value.selectedPaymentMethod ?: return
        val ref = _uiState.value.paymentReference

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val res = createProtectionRequestUseCase(customerNumberId, pkg.id, pm.id, ref)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, isSubmitted = true)
                is AppResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = res.error.message)
            }
        }
    }

    fun submitRenewal(protectionId: String) {
        val pkg = _uiState.value.defaultPackage ?: return
        val pm = _uiState.value.selectedPaymentMethod ?: return
        val ref = _uiState.value.paymentReference

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val res = createRenewalRequestUseCase(protectionId, pkg.id, pm.id, ref)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, isSubmitted = true)
                is AppResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = res.error.message)
            }
        }
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(isSubmitted = false, errorMessage = null, paymentReference = "")
    }
}
