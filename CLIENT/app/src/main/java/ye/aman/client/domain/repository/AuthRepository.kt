package ye.aman.client.domain.repository

import kotlinx.coroutines.flow.Flow
import ye.aman.client.core.result.AppResult
import ye.aman.client.domain.model.User

interface AuthRepository {
    val currentUser: Flow<User?>
    val isAuthenticated: Flow<Boolean>

    suspend fun login(email: String, password: String): AppResult<User>
    suspend fun register(fullName: String, email: String, phone: String, password: String): AppResult<User>
    suspend fun restoreSession(): AppResult<User?>
    suspend fun resetPassword(email: String): AppResult<Unit>
    suspend fun changePassword(currentPassword: String, newPassword: String): AppResult<Unit>
    suspend fun updateProfile(fullName: String, phone: String?): AppResult<User>
    suspend fun logout(): AppResult<Unit>
}
