package ye.aman.admin.domain.repository

import kotlinx.coroutines.flow.Flow
import ye.aman.admin.core.result.AppResult
import ye.aman.admin.domain.model.*

interface AdminAuthRepository {
    suspend fun login(email: String, password: String): AppResult<AdminUser>
    suspend fun resetPassword(email: String): AppResult<Unit>
    suspend fun getCurrentUser(): AppResult<AdminUser>
    suspend fun logout(): AppResult<Unit>
}

interface AdminDashboardRepository {
    suspend fun getDashboardSummary(): AppResult<AdminDashboardSummary>
}

interface AdminCustomerRepository {
    fun getCustomers(): Flow<List<CustomerRecord>>
    suspend fun refreshCustomers(): AppResult<Unit>
    suspend fun getCustomerDetails(customerId: String): AppResult<CustomerRecord>
    suspend fun updateCustomerStatus(customerId: String, status: String): AppResult<Unit>
}

interface AdminNumberRepository {
    fun getNumbers(): Flow<List<AdminPhoneNumber>>
    suspend fun refreshNumbers(): AppResult<Unit>
    suspend fun getNumberDetails(numberId: String): AppResult<AdminPhoneNumber>
}

interface AdminOperationsRepository {
    // Protection Requests
    fun getProtectionRequests(): Flow<List<AdminProtectionRequest>>
    suspend fun refreshProtectionRequests(): AppResult<Unit>
    suspend fun approveProtectionRequest(requestId: String): AppResult<Unit>
    suspend fun rejectProtectionRequest(requestId: String, reason: String): AppResult<Unit>

    // Protections & Renewals
    fun getProtections(): Flow<List<AdminProtection>>
    suspend fun refreshProtections(): AppResult<Unit>
    suspend fun approveRenewal(requestId: String): AppResult<Unit>
    suspend fun rejectRenewal(requestId: String, reason: String): AppResult<Unit>

    // Operational Tasks
    fun getPaymentTasks(): Flow<List<PaymentTask>>
    suspend fun refreshPaymentTasks(): AppResult<Unit>
    suspend fun executeTask(taskId: String, notes: String): AppResult<Unit>
    suspend fun rescheduleTask(taskId: String, newDueAt: String, reason: String): AppResult<Unit>
}

interface AdminSettingsRepository {
    // Providers
    fun getProviders(): Flow<List<TelecomProvider>>
    suspend fun refreshProviders(): AppResult<Unit>
    suspend fun upsertProvider(id: String?, name: String, code: String, numberLength: Int, isActive: Boolean): AppResult<Unit>
    suspend fun upsertPrefix(id: String?, providerId: String, prefix: String, isActive: Boolean): AppResult<Unit>

    // Packages
    fun getPackages(): Flow<List<Package>>
    suspend fun refreshPackages(): AppResult<Unit>
    suspend fun upsertPackage(id: String?, name: String, price: Double, durationDays: Int, isActive: Boolean, isVisible: Boolean): AppResult<Unit>

    // Payment Methods
    fun getPaymentMethods(): Flow<List<PaymentMethod>>
    suspend fun refreshPaymentMethods(): AppResult<Unit>
    suspend fun upsertPaymentMethod(id: String?, name: String, type: String, accountNumber: String, accountHolder: String, isActive: Boolean): AppResult<Unit>

    // System Settings & Audit Logs
    suspend fun getSystemSettings(): AppResult<List<SystemSetting>>
    suspend fun updateSystemSetting(key: String, jsonValue: String): AppResult<Unit>
    suspend fun getAuditLogs(): AppResult<List<AuditLog>>
}
