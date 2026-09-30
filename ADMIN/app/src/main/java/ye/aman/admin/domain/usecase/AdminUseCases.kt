package ye.aman.admin.domain.usecase

import kotlinx.coroutines.flow.Flow
import ye.aman.admin.core.error.AppError
import ye.aman.admin.core.result.AppResult
import ye.aman.admin.core.security.SecurityUtils
import ye.aman.admin.domain.model.*
import ye.aman.admin.domain.repository.*

class AdminLoginUseCase(private val repository: AdminAuthRepository) {
    suspend operator fun invoke(email: String, pass: String): AppResult<AdminUser> {
        val cleanEmail = SecurityUtils.sanitizeInput(email)
        if (!SecurityUtils.isValidEmail(cleanEmail)) {
            return AppResult.Error(AppError.ValidationError("يرجى إدخال بريد إلكتروني صحيح"))
        }
        if (pass.isBlank()) {
            return AppResult.Error(AppError.ValidationError("يرجى إدخال كلمة المرور"))
        }
        return repository.login(cleanEmail, pass)
    }
}

class AdminResetPasswordUseCase(private val repository: AdminAuthRepository) {
    suspend operator fun invoke(email: String): AppResult<Unit> {
        val cleanEmail = SecurityUtils.sanitizeInput(email)
        if (!SecurityUtils.isValidEmail(cleanEmail)) {
            return AppResult.Error(AppError.ValidationError("بريد إلكتروني غير صالح"))
        }
        return repository.resetPassword(cleanEmail)
    }
}

class GetAdminDashboardSummaryUseCase(private val repository: AdminDashboardRepository) {
    suspend operator fun invoke(): AppResult<AdminDashboardSummary> {
        return repository.getDashboardSummary()
    }
}

class GetCustomersUseCase(private val repository: AdminCustomerRepository) {
    operator fun invoke(): Flow<List<CustomerRecord>> = repository.getCustomers()
    suspend fun refresh(): AppResult<Unit> = repository.refreshCustomers()
}

class UpdateCustomerStatusUseCase(private val repository: AdminCustomerRepository) {
    suspend operator fun invoke(customerId: String, status: String): AppResult<Unit> {
        if (customerId.isBlank()) return AppResult.Error(AppError.ValidationError("معرف العميل غير صالح"))
        return repository.updateCustomerStatus(customerId, status)
    }
}

class GetAdminNumbersUseCase(private val repository: AdminNumberRepository) {
    operator fun invoke(): Flow<List<AdminPhoneNumber>> = repository.getNumbers()
    suspend fun refresh(): AppResult<Unit> = repository.refreshNumbers()
}

class OperationsUseCases(private val repository: AdminOperationsRepository) {
    fun getProtectionRequests(): Flow<List<AdminProtectionRequest>> = repository.getProtectionRequests()
    suspend fun refreshRequests(): AppResult<Unit> = repository.refreshProtectionRequests()

    suspend fun approveRequest(requestId: String): AppResult<Unit> {
        if (requestId.isBlank()) return AppResult.Error(AppError.ValidationError("معرف الطلب غير صالح"))
        return repository.approveProtectionRequest(requestId)
    }

    suspend fun rejectRequest(requestId: String, reason: String): AppResult<Unit> {
        if (requestId.isBlank()) return AppResult.Error(AppError.ValidationError("معرف الطلب غير صالح"))
        if (reason.isBlank()) return AppResult.Error(AppError.ValidationError("يجب كتابة سبب الرفض الإلزامي"))
        return repository.rejectProtectionRequest(requestId, reason.trim())
    }

    fun getProtections(): Flow<List<AdminProtection>> = repository.getProtections()
    suspend fun refreshProtections(): AppResult<Unit> = repository.refreshProtections()

    suspend fun approveRenewal(requestId: String): AppResult<Unit> {
        if (requestId.isBlank()) return AppResult.Error(AppError.ValidationError("معرف الطلب غير صالح"))
        return repository.approveRenewal(requestId)
    }

    suspend fun rejectRenewal(requestId: String, reason: String): AppResult<Unit> {
        if (reason.isBlank()) return AppResult.Error(AppError.ValidationError("يجب إدخال سبب الرفض الإلزامي"))
        return repository.rejectRenewal(requestId, reason.trim())
    }

    fun getTasks(): Flow<List<PaymentTask>> = repository.getPaymentTasks()
    suspend fun refreshTasks(): AppResult<Unit> = repository.refreshPaymentTasks()

    suspend fun executeTask(taskId: String, notes: String): AppResult<Unit> {
        if (taskId.isBlank()) return AppResult.Error(AppError.ValidationError("معرف المهمة غير صالح"))
        return repository.executeTask(taskId, notes)
    }

    suspend fun rescheduleTask(taskId: String, newDueAt: String, reason: String): AppResult<Unit> {
        if (taskId.isBlank()) return AppResult.Error(AppError.ValidationError("معرف المهمة غير صالح"))
        if (reason.isBlank()) return AppResult.Error(AppError.ValidationError("يجب إدخال سبب إعادة الجدولة الإلزامي"))
        return repository.rescheduleTask(taskId, newDueAt, reason.trim())
    }
}

class SettingsUseCases(private val repository: AdminSettingsRepository) {
    fun getProviders(): Flow<List<TelecomProvider>> = repository.getProviders()
    suspend fun refreshProviders(): AppResult<Unit> = repository.refreshProviders()
    suspend fun upsertProvider(id: String?, name: String, code: String, len: Int, active: Boolean) =
        repository.upsertProvider(id, name, code, len, active)

    fun getPackages(): Flow<List<Package>> = repository.getPackages()
    suspend fun refreshPackages(): AppResult<Unit> = repository.refreshPackages()
    suspend fun upsertPackage(id: String?, name: String, price: Double, days: Int, active: Boolean, visible: Boolean) =
        repository.upsertPackage(id, name, price, days, active, visible)

    fun getPaymentMethods(): Flow<List<PaymentMethod>> = repository.getPaymentMethods()
    suspend fun refreshPaymentMethods(): AppResult<Unit> = repository.refreshPaymentMethods()
    suspend fun upsertPaymentMethod(id: String?, name: String, type: String, accNum: String, accHolder: String, active: Boolean) =
        repository.upsertPaymentMethod(id, name, type, accNum, accHolder, active)

    suspend fun getAuditLogs(): AppResult<List<AuditLog>> = repository.getAuditLogs()
    suspend fun getSystemSettings(): AppResult<List<SystemSetting>> = repository.getSystemSettings()
    suspend fun updateSystemSetting(key: String, jsonVal: String) = repository.updateSystemSetting(key, jsonVal)
}
