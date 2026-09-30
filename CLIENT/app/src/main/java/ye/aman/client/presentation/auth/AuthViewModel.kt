package ye.aman.client.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ye.aman.client.core.result.AppResult
import ye.aman.client.domain.usecase.LoginUseCase
import ye.aman.client.domain.usecase.RegisterUseCase
import ye.aman.client.domain.usecase.ResetPasswordUseCase

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val resetSent: Boolean = false
)

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "يرجى إدخال البريد الإلكتروني وكلمة المرور")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val res = loginUseCase(email, pass)) {
                is AppResult.Success -> _uiState.value = AuthUiState(isSuccess = true)
                is AppResult.Error -> _uiState.value = AuthUiState(errorMessage = res.error.message)
            }
        }
    }

    fun register(name: String, email: String, phone: String, pass: String, confirmPass: String) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "يرجى تعبئة الحقول المطلوبة")
            return
        }
        if (pass != confirmPass) {
            _uiState.value = AuthUiState(errorMessage = "كلمتا المرور غير متطابقتين")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val res = registerUseCase(name, email, phone, pass)) {
                is AppResult.Success -> _uiState.value = AuthUiState(isSuccess = true)
                is AppResult.Error -> _uiState.value = AuthUiState(errorMessage = res.error.message)
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState(errorMessage = "يرجى إدخال البريد الإلكتروني")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val res = resetPasswordUseCase(email)) {
                is AppResult.Success -> _uiState.value = AuthUiState(resetSent = true)
                is AppResult.Error -> _uiState.value = AuthUiState(errorMessage = res.error.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
