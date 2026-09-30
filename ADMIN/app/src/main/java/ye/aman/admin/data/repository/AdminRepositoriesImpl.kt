package ye.aman.admin.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ye.aman.admin.core.error.AppError
import ye.aman.admin.core.result.AppResult
import ye.aman.admin.core.session.SessionManager
import ye.aman.admin.data.local.AmanAdminDatabase
import ye.aman.admin.data.mapper.AdminMappers.toDomain
import ye.aman.admin.data.mapper.AdminMappers.toEntity
import ye.aman.admin.data.remote.SupabaseAdminRemoteDataSource
import ye.aman.admin.domain.model.*
import ye.aman.admin.domain.repository.*

class AdminAuthRepositoryImpl(
    private val remoteDataSource: SupabaseAdminRemoteDataSource,
    private val sessionManager: SessionManager
) : AdminAuthRepository {

    override suspend fun login(email: String, password: String): AppResult<AdminUser> {
        val result = remoteDataSource.login(email, password)
        return when (result) {
            is AppResult.Success -> {
                val dto = result.data
                sessionManager.saveSession(
                    token = "session_active_${dto.id}",
                    userId = dto.id,
                    email = dto.email,
                    name = dto.fullName,
                    role = dto.role
                )
                AppResult.Success(
                    AdminUser(
                        id = dto.id,
                        fullName = dto.fullName,
                        email = dto.email,
                        phone = dto.phone,
                        userType = dto.userType,
                        status = dto.status,
                        role = dto.role,
                        createdAt = dto.createdAt
                    )
                )
            }
            is AppResult.Error -> result
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun resetPassword(email: String): AppResult<Unit> =
        remoteDataSource.resetPassword(email)

    override suspend fun getCurrentUser(): AppResult<AdminUser> {
        val id = sessionManager.getUserId() ?: return AppResult.Error(AppError.SessionExpired())
        return AppResult.Success(
            AdminUser(
                id = id,
                fullName = sessionManager.getUserName() ?: "المشرف",
                email = sessionManager.getUserEmail() ?: "",
                phone = "",
                userType = "admin",
                status = "active",
                role = sessionManager.getUserRole() ?: "admin",
                createdAt = ""
            )
        )
    }

    override suspend fun logout(): AppResult<Unit> {
        sessionManager.clearSession()
        return AppResult.Success(Unit)
    }
}

class AdminDashboardRepositoryImpl(
    private val remoteDataSource: SupabaseAdminRemoteDataSource
) : AdminDashboardRepository {

    override suspend fun getDashboardSummary(): AppResult<AdminDashboardSummary> {
        val res = remoteDataSource.getDashboardSummary()
        return when (res) {
            is AppResult.Success -> AppResult.Success(
                AdminDashboardSummary(
                    totalCustomers = res.data.totalCustomers,
                    activeProtections = res.data.activeProtections,
                    pendingRequests = res.data.pendingRequests,
                    dueTasksCount = res.data.dueTasksCount,
                    totalRevenue = res.data.totalRevenue,
                    totalNumbers = res.data.totalNumbers
                )
            )
            is AppResult.Error -> res
            is AppResult.Loading -> AppResult.Loading
        }
    }
}

class AdminCustomerRepositoryImpl(
    private val remoteDataSource: SupabaseAdminRemoteDataSource,
    private val database: AmanAdminDatabase
) : AdminCustomerRepository {

    override fun getCustomers(): Flow<List<CustomerRecord>> {
        return database.customerDao().getAllCustomers().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun refreshCustomers(): AppResult<Unit> {
        val res = remoteDataSource.getCustomers()
        return when (res) {
            is AppResult.Success -> {
                val entities = res.data.map { it.toEntity() }
                database.customerDao().deleteAll()
                database.customerDao().insertAll(entities)
                AppResult.Success(Unit)
            }
            is AppResult.Error -> res
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getCustomerDetails(customerId: String): AppResult<CustomerRecord> {
        val entity = database.customerDao().getCustomerById(customerId)
        return if (entity != null) {
            AppResult.Success(entity.toDomain())
        } else {
            AppResult.Error(AppError.ServerError(message = "العميل غير موجود في السجل"))
        }
    }

    override suspend fun updateCustomerStatus(customerId: String, status: String): AppResult<Unit> {
        val res = remoteDataSource.updateCustomerStatus(customerId, status)
        if (res is AppResult.Success) {
            database.customerDao().updateStatus(customerId, status)
        }
        return res
    }
}

class AdminNumberRepositoryImpl(
    private val remoteDataSource: SupabaseAdminRemoteDataSource,
    private val database: AmanAdminDatabase
) : AdminNumberRepository {

    override fun getNumbers(): Flow<List<AdminPhoneNumber>> {
        return database.phoneNumberDao().getAllNumbers().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun refreshNumbers(): AppResult<Unit> {
        val res = remoteDataSource.getNumbers()
        return when (res) {
            is AppResult.Success -> {
                val entities = res.data.map { it.toEntity() }
                database.phoneNumberDao().deleteAll()
                database.phoneNumberDao().insertAll(entities)
                AppResult.Success(Unit)
            }
            is AppResult.Error -> res
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun getNumberDetails(numberId: String): AppResult<AdminPhoneNumber> {
        val entity = database.phoneNumberDao().getById(numberId)
        return if (entity != null) {
            AppResult.Success(entity.toDomain())
        } else {
            AppResult.Error(AppError.ServerError(message = "الرقم غير مسجل"))
        }
    }
}

class AdminOperationsRepositoryImpl(
    private val remoteDataSource: SupabaseAdminRemoteDataSource,
    private val database: AmanAdminDatabase
) : AdminOperationsRepository {

    override fun getProtectionRequests(): Flow<List<AdminProtectionRequest>> {
        return database.protectionRequestDao().getAllRequests().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun refreshProtectionRequests(): AppResult<Unit> {
        val res = remoteDataSource.getProtectionRequests()
        return when (res) {
            is AppResult.Success -> {
                val entities = res.data.map { it.toEntity() }
                database.protectionRequestDao().deleteAll()
                database.protectionRequestDao().insertAll(entities)
                AppResult.Success(Unit)
            }
            is AppResult.Error -> res
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun approveProtectionRequest(requestId: String): AppResult<Unit> {
        val res = remoteDataSource.approveProtectionRequest(requestId)
        if (res is AppResult.Success) {
            database.protectionRequestDao().updateStatus(requestId, "approved")
        }
        return res
    }

    override suspend fun rejectProtectionRequest(requestId: String, reason: String): AppResult<Unit> {
        val res = remoteDataSource.rejectProtectionRequest(requestId, reason)
        if (res is AppResult.Success) {
            database.protectionRequestDao().updateStatus(requestId, "rejected")
        }
        return res
    }

    override fun getProtections(): Flow<List<AdminProtection>> {
        return database.protectionDao().getAllProtections().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun refreshProtections(): AppResult<Unit> {
        val res = remoteDataSource.getProtections()
        return when (res) {
            is AppResult.Success -> {
                val entities = res.data.map { it.toEntity() }
                database.protectionDao().deleteAll()
                database.protectionDao().insertAll(entities)
                AppResult.Success(Unit)
            }
            is AppResult.Error -> res
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun approveRenewal(requestId: String): AppResult<Unit> =
        remoteDataSource.approveRenewal(requestId)

    override suspend fun rejectRenewal(requestId: String, reason: String): AppResult<Unit> =
        remoteDataSource.rejectRenewal(requestId, reason)

    override fun getPaymentTasks(): Flow<List<PaymentTask>> {
        return database.paymentTaskDao().getAllTasks().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun refreshPaymentTasks(): AppResult<Unit> {
        val res = remoteDataSource.getPaymentTasks()
        return when (res) {
            is AppResult.Success -> {
                val entities = res.data.map { it.toEntity() }
                database.paymentTaskDao().deleteAll()
                database.paymentTaskDao().insertAll(entities)
                AppResult.Success(Unit)
            }
            is AppResult.Error -> res
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun executeTask(taskId: String, notes: String): AppResult<Unit> {
        val res = remoteDataSource.executeTask(taskId, notes)
        if (res is AppResult.Success) {
            database.paymentTaskDao().markCompleted(taskId, notes)
        }
        return res
    }

    override suspend fun rescheduleTask(taskId: String, newDueAt: String, reason: String): AppResult<Unit> =
        remoteDataSource.rescheduleTask(taskId, newDueAt, reason)
}

class AdminSettingsRepositoryImpl(
    private val remoteDataSource: SupabaseAdminRemoteDataSource
) : AdminSettingsRepository {

    override fun getProviders(): Flow<List<TelecomProvider>> {
        return kotlinx.coroutines.flow.flow {
            val res = remoteDataSource.getProviders()
            if (res is AppResult.Success) {
                emit(res.data.map { it.toDomain() })
            } else {
                emit(emptyList())
            }
        }
    }

    override suspend fun refreshProviders(): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun upsertProvider(id: String?, name: String, code: String, len: Int, active: Boolean) =
        remoteDataSource.upsertProvider(id, name, code, len, active)

    override suspend fun upsertPrefix(id: String?, providerId: String, prefix: String, active: Boolean) =
        AppResult.Success(Unit)

    override fun getPackages(): Flow<List<Package>> {
        return kotlinx.coroutines.flow.flow {
            val res = remoteDataSource.getPackages()
            if (res is AppResult.Success) {
                emit(res.data.map { it.toDomain() })
            } else {
                emit(emptyList())
            }
        }
    }

    override suspend fun refreshPackages(): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun upsertPackage(id: String?, name: String, price: Double, days: Int, active: Boolean, visible: Boolean) =
        remoteDataSource.upsertPackage(id, name, price, days, active, visible)

    override fun getPaymentMethods(): Flow<List<PaymentMethod>> {
        return kotlinx.coroutines.flow.flow {
            val res = remoteDataSource.getPaymentMethods()
            if (res is AppResult.Success) {
                emit(res.data.map { it.toDomain() })
            } else {
                emit(emptyList())
            }
        }
    }

    override suspend fun refreshPaymentMethods(): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun upsertPaymentMethod(id: String?, name: String, type: String, accNum: String, accHolder: String, active: Boolean) =
        remoteDataSource.upsertPaymentMethod(id, name, type, accNum, accHolder, active)

    override suspend fun getSystemSettings(): AppResult<List<SystemSetting>> {
        val res = remoteDataSource.getSystemSettings()
        return when (res) {
            is AppResult.Success -> AppResult.Success(res.data.map { SystemSetting(it.key, it.value, it.description) })
            is AppResult.Error -> res
            is AppResult.Loading -> AppResult.Loading
        }
    }

    override suspend fun updateSystemSetting(key: String, jsonValue: String) =
        remoteDataSource.updateSystemSetting(key, jsonValue)

    override suspend fun getAuditLogs(): AppResult<List<AuditLog>> {
        val res = remoteDataSource.getAuditLogs()
        return when (res) {
            is AppResult.Success -> AppResult.Success(res.data.map {
                AuditLog(it.id, it.actorId, it.actorName, it.action, it.entityName, it.entityId, it.changes, it.createdAt)
            })
            is AppResult.Error -> res
            is AppResult.Loading -> AppResult.Loading
        }
    }
}
