package ye.aman.client.domain.usecase

import ye.aman.client.core.result.AppResult
import ye.aman.client.domain.model.User
import ye.aman.client.domain.repository.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, pass: String): AppResult<User> =
        authRepository.login(email.trim(), pass)
}

class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(name: String, email: String, phone: String, pass: String): AppResult<User> =
        authRepository.register(name.trim(), email.trim(), phone.trim(), pass)
}

class RestoreSessionUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): AppResult<User?> =
        authRepository.restoreSession()
}

class ResetPasswordUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String): AppResult<Unit> =
        authRepository.resetPassword(email.trim())
}

class ChangePasswordUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(currentPass: String, newPass: String): AppResult<Unit> =
        authRepository.changePassword(currentPass, newPass)
}

class UpdateAccountUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(fullName: String, phone: String?): AppResult<User> =
        authRepository.updateProfile(fullName.trim(), phone?.trim())
}

class LogoutUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): AppResult<Unit> =
        authRepository.logout()
}
