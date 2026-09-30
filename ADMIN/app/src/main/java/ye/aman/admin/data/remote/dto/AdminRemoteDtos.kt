package ye.aman.admin.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminDashboardSummaryDto(
    @SerialName("total_customers") val totalCustomers: Int = 0,
    @SerialName("active_protections") val activeProtections: Int = 0,
    @SerialName("pending_requests") val pendingRequests: Int = 0,
    @SerialName("due_tasks_count") val dueTasksCount: Int = 0,
    @SerialName("total_revenue") val totalRevenue: Double = 0.0,
    @SerialName("total_numbers") val totalNumbers: Int = 0
)

@Serializable
data class CustomerDto(
    val id: String,
    @SerialName("full_name") val fullName: String,
    val phone: String,
    val email: String? = null,
    val status: String = "active",
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class AdminPhoneNumberDto(
    val id: String,
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("provider_id") val providerId: String,
    val status: String = "unassigned",
    @SerialName("provider_name") val providerName: String? = null,
    @SerialName("provider_code") val providerCode: String? = null,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("customer_name") val customerName: String? = null,
    @SerialName("is_protected") val isProtected: Boolean = false,
    @SerialName("protection_expires_at") val protectionExpiresAt: String? = null
)

@Serializable
data class AdminProtectionRequestDto(
    val id: String,
    @SerialName("request_number") val requestNumber: String,
    @SerialName("customer_id") val customerId: String,
    @SerialName("customer_name") val customerName: String? = "عميل",
    @SerialName("customer_phone") val customerPhone: String? = "",
    @SerialName("phone_number_id") val phoneNumberId: String,
    @SerialName("phone_number") val phoneNumber: String? = "",
    @SerialName("provider_name") val providerName: String? = "",
    @SerialName("package_id") val packageId: String,
    @SerialName("package_name") val packageName: String? = "باقة سنوية",
    @SerialName("package_price") val packagePrice: Double = 1000.0,
    @SerialName("request_type") val requestType: String,
    val status: String,
    @SerialName("payment_method_id") val paymentMethodId: String,
    @SerialName("payment_method_name") val paymentMethodName: String? = "حوالة",
    @SerialName("transfer_number") val transferNumber: String,
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class AdminProtectionDto(
    val id: String,
    @SerialName("customer_id") val customerId: String,
    @SerialName("customer_name") val customerName: String? = "عميل",
    @SerialName("phone_number_id") val phoneNumberId: String,
    @SerialName("phone_number") val phoneNumber: String? = "",
    @SerialName("provider_name") val providerName: String? = "",
    @SerialName("package_name") val packageName: String? = "باقة سنوية",
    val status: String,
    @SerialName("started_at") val startedAt: String,
    @SerialName("expires_at") val expiresAt: String,
    @SerialName("auto_renew") val autoRenew: Boolean = false
)

@Serializable
data class PaymentTaskDto(
    val id: String,
    @SerialName("protection_id") val protectionId: String,
    @SerialName("phone_number") val phoneNumber: String? = "",
    @SerialName("provider_name") val providerName: String? = "",
    @SerialName("customer_name") val customerName: String? = "",
    @SerialName("due_at") val dueAt: String,
    @SerialName("original_due_at") val originalDueAt: String? = null,
    val status: String,
    @SerialName("rescheduled_at") val rescheduledAt: String? = null,
    @SerialName("reschedule_reason") val rescheduleReason: String? = null,
    @SerialName("execution_result") val executionResult: String? = null,
    @SerialName("executed_at") val executedAt: String? = null
)

@Serializable
data class TelecomProviderDto(
    val id: String,
    val name: String,
    val code: String,
    @SerialName("number_length") val numberLength: Int = 9,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0
)

@Serializable
data class ProviderPrefixDto(
    val id: String,
    @SerialName("provider_id") val providerId: String,
    val prefix: String,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class PackageDto(
    val id: String,
    val name: String,
    val price: Double,
    val currency: String = "YER",
    @SerialName("duration_days") val durationDays: Int = 365,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("is_visible") val isVisible: Boolean = true
)

@Serializable
data class PaymentMethodDto(
    val id: String,
    val name: String,
    val type: String,
    @SerialName("account_number") val accountNumber: String,
    @SerialName("account_holder") val accountHolder: String,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class AuditLogDto(
    val id: String,
    @SerialName("actor_id") val actorId: String,
    @SerialName("actor_name") val actorName: String? = null,
    val action: String,
    @SerialName("entity_name") val entityName: String,
    @SerialName("entity_id") val entityId: String? = null,
    val changes: String? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class SystemSettingDto(
    val key: String,
    val value: String,
    val description: String? = null
)
