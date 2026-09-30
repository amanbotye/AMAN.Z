package ye.aman.client.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ye.aman.client.core.result.AppResult
import ye.aman.client.core.session.SessionManager
import ye.aman.client.data.local.dao.UserDao
import ye.aman.client.data.local.entity.UserEntity
import ye.aman.client.data.mapper.Mappers.toDomain
import ye.aman.client.domain.model.User
import ye.aman.client.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) : AuthRepository {

    override val currentUser: Flow<User?> =
        userDao.getUser(sessionManager.currentUserId.value ?: "").map { it?.toDomain() }

    override val isAuthenticated: Flow<Boolean> = sessionManager.isAuthenticated

    override suspend fun login(email: String, password: String): AppResult<User> {
        val userId = "user_" + email.hashCode()
        val userEntity = UserEntity(
            id = userId,
            fullName = sessionManager.getSavedFullName() ?: "عميل أمان",
            email = email,
            phone = null,
            status = "active",
            userType = "customer",
            createdAt = java.time.Instant.now().toString()
        )
        userDao.insertUser(userEntity)
        sessionManager.saveSession(userId, email, userEntity.fullName)
        return AppResult.Success(userEntity.toDomain())
    }

    override suspend fun register(fullName: String, email: String, phone: String, password: String): AppResult<User> {
        val userId = "user_" + email.hashCode()
        val userEntity = UserEntity(
            id = userId,
            fullName = fullName,
            email = email,
            phone = phone,
            status = "active",
            userType = "customer",
            createdAt = java.time.Instant.now().toString()
        )
        userDao.insertUser(userEntity)
        sessionManager.saveSession(userId, email, fullName)
        return AppResult.Success(userEntity.toDomain())
    }

    override suspend fun restoreSession(): AppResult<User?> {
        val userId = sessionManager.currentUserId.value
        return if (userId != null) {
            val email = sessionManager.getSavedEmail() ?: "user@aman.ye"
            val name = sessionManager.getSavedFullName() ?: "عميل أمان"
            val user = User(userId, name, email, null, "active", "customer", "")
            AppResult.Success(user)
        } else {
            AppResult.Success(null)
        }
    }

    override suspend fun resetPassword(email: String): AppResult<Unit> =
        AppResult.Success(Unit)

    override suspend fun changePassword(currentPassword: String, newPassword: String): AppResult<Unit> =
        AppResult.Success(Unit)

    override suspend fun updateProfile(fullName: String, phone: String?): AppResult<User> {
        val userId = sessionManager.currentUserId.value ?: ""
        val email = sessionManager.getSavedEmail() ?: ""
        val userEntity = UserEntity(userId, fullName, email, phone, "active", "customer", "")
        userDao.insertUser(userEntity)
        sessionManager.saveSession(userId, email, fullName)
        return AppResult.Success(userEntity.toDomain())
    }

    override suspend fun logout(): AppResult<Unit> {
        sessionManager.clearSession()
        userDao.clear()
        return AppResult.Success(Unit)
    }
}
