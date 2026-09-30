package ye.aman.client.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ye.aman.client.domain.model.User
import ye.aman.client.domain.repository.AuthRepository
import ye.aman.client.domain.usecase.ChangePasswordUseCase
import ye.aman.client.domain.usecase.LogoutUseCase
import ye.aman.client.domain.usecase.UpdateAccountUseCase

data class SettingsUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
    val isLoggedOut: Boolean = false
)

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val updateAccountUseCase: UpdateAccountUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { u ->
                _uiState.value = _uiState.value.copy(user = u)
            }
        }
    }

    fun updateProfile(name: String, phone: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            updateAccountUseCase(name, phone)
            _uiState.value = _uiState.value.copy(isLoading = false, message = "تم تحديث البيانات بنجاح")
        }
    }

    fun changePassword(curr: String, newPass: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            changePasswordUseCase(curr, newPass)
            _uiState.value = _uiState.value.copy(isLoading = false, message = "تم تغيير كلمة المرور بنجاح")
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.value = _uiState.value.copy(isLoggedOut = true)
        }
    }
}
