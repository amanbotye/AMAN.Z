package ye.aman.admin.domain.model

data class AdminDashboardSummary(
    val totalCustomers: Int,
    val activeProtections: Int,
    val pendingRequests: Int,
    val dueTasksCount: Int,
    val totalRevenue: Double,
    val totalNumbers: Int
)

data class AdminUser(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val userType: String,
    val status: String,
    val role: String,
    val createdAt: String
)

data class CustomerRecord(
    val id: String,
    val fullName: String,
    val phone: String,
    val email: String?,
    val status: String,
    val numbersCount: Int,
    val protectionsCount: Int,
    val createdAt: String
)

data class AdminPhoneNumber(
    val id: String,
    val phoneNumber: String,
    val providerId: String,
    val providerName: String,
    val providerCode: String,
    val status: String,
    val customerId: String?,
    val customerName: String?,
    val isProtected: Boolean,
    val protectionExpiresAt: String?
)

data class AdminProtectionRequest(
    val id: String,
    val requestNumber: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val phoneNumberId: String,
    val phoneNumber: String,
    val providerName: String,
    val packageId: String,
    val packageName: String,
    val packagePrice: Double,
    val requestType: String, // new_protection, renewal
    val status: String, // pending, approved, rejected, conflict
    val paymentMethodId: String,
    val paymentMethodName: String,
    val transferNumber: String,
    val rejectionReason: String?,
    val createdAt: String
)

data class AdminProtection(
    val id: String,
    val customerId: String,
    val customerName: String,
    val phoneNumberId: String,
    val phoneNumber: String,
    val providerName: String,
    val packageName: String,
    val status: String, // active, expired, renewed, cancelled
    val startedAt: String,
    val expiresAt: String,
    val autoRenew: Boolean,
    val remainingDays: Int
)

data class PaymentTask(
    val id: String,
    val protectionId: String,
    val phoneNumber: String,
    val providerName: String,
    val customerName: String,
    val dueAt: String,
    val originalDueAt: String?,
    val status: String, // due, completed
    val rescheduledAt: String?,
    val rescheduleReason: String?,
    val executionResult: String?,
    val executedAt: String?
)

data class PeriodicPaymentPlan(
    val id: String,
    val protectionId: String,
    val providerId: String,
    val intervalDays: Int,
    val amountPerTask: Double,
    val totalTasksPlanned: Int,
    val completedTasksCount: Int,
    val isActive: Boolean
)

data class TelecomProvider(
    val id: String,
    val name: String,
    val code: String,
    val numberLength: Int,
    val isActive: Boolean,
    val sortOrder: Int,
    val prefixes: List<String>
)

data class Package(
    val id: String,
    val name: String,
    val price: Double,
    val currency: String,
    val durationDays: Int,
    val isActive: Boolean,
    val isVisible: Boolean
)

data class PaymentMethod(
    val id: String,
    val name: String,
    val type: String,
    val accountNumber: String,
    val accountHolder: String,
    val isActive: Boolean
)

data class AuditLog(
    val id: String,
    val actorId: String,
    val actorName: String?,
    val action: String,
    val entityName: String,
    val entityId: String?,
    val changes: String?,
    val createdAt: String
)

data class SystemSetting(
    val key: String,
    val value: String,
    val description: String?
)
