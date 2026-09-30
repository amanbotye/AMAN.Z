package ye.aman.client.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    @SerialName("full_name") val fullName: String,
    val email: String,
    val phone: String? = null,
    val status: String = "active",
    @SerialName("user_type") val userType: String = "customer",
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class CustomerNumberDto(
    @SerialName("customer_number_id") val customerNumberId: String,
    @SerialName("customer_id") val customerId: String,
    @SerialName("phone_number_id") val phoneNumberId: String,
    @SerialName("normalized_number") val normalizedNumber: String,
    @SerialName("display_number") val displayNumber: String,
    @SerialName("provider_id") val providerId: String,
    @SerialName("provider_name") val providerName: String,
    @SerialName("provider_code") val providerCode: String,
    @SerialName("custom_label") val customLabel: String? = null,
    @SerialName("is_protected") val isProtected: Boolean = false,
    @SerialName("protection_id") val protectionId: String? = null,
    @SerialName("protection_status") val protectionStatus: String? = null,
    @SerialName("protection_start_at") val protectionStartAt: String? = null,
    @SerialName("protection_end_at") val protectionEndAt: String? = null,
    @SerialName("days_remaining") val daysRemaining: Int = 0,
    @SerialName("package_name_snapshot") val packageNameSnapshot: String? = null,
    @SerialName("package_price_snapshot") val packagePriceSnapshot: Double? = null,
    @SerialName("package_currency_snapshot") val packageCurrencySnapshot: String? = null,
    @SerialName("added_at") val addedAt: String,
    @SerialName("customer_number_active") val customerNumberActive: Boolean = true
)

@Serializable
data class PackageDto(
    val id: String,
    @SerialName("provider_id") val providerId: String? = null,
    val name: String,
    val description: String? = null,
    @SerialName("duration_days") val durationDays: Int,
    val price: Double,
    val currency: String = "YER",
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("is_visible") val isVisible: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0
)

@Serializable
data class PaymentMethodDto(
    val id: String,
    val type: String,
    val name: String,
    @SerialName("recipient_name") val recipientName: String,
    @SerialName("account_number") val accountNumber: String,
    val instructions: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0
)

@Serializable
data class ProtectionDto(
    val id: String,
    @SerialName("customer_id") val customerId: String,
    @SerialName("phone_number_id") val phoneNumberId: String,
    @SerialName("request_id") val requestId: String,
    @SerialName("package_id") val packageId: String,
    @SerialName("start_at") val startAt: String,
    @SerialName("end_at") val endAt: String,
    val status: String = "active",
    @SerialName("package_name_snapshot") val packageNameSnapshot: String,
    @SerialName("package_price_snapshot") val packagePriceSnapshot: Double,
    @SerialName("package_currency_snapshot") val packageCurrencySnapshot: String,
    @SerialName("provider_name_snapshot") val providerNameSnapshot: String
)

@Serializable
data class ProtectionRequestDto(
    val id: String,
    @SerialName("customer_id") val customerId: String,
    @SerialName("phone_number_id") val phoneNumberId: String,
    @SerialName("package_name_snapshot") val packageName: String,
    @SerialName("package_price_snapshot") val amount: Double,
    @SerialName("package_currency_snapshot") val currency: String,
    @SerialName("provider_name_snapshot") val providerName: String,
    @SerialName("payment_method_name_snapshot") val paymentMethodName: String,
    @SerialName("payment_reference") val paymentReference: String,
    @SerialName("request_type") val requestType: String,
    @SerialName("previous_protection_id") val previousProtectionId: String? = null,
    val status: String = "pending",
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    @SerialName("submitted_at") val submittedAt: String,
    @SerialName("reviewed_at") val reviewedAt: String? = null
)

@Serializable
data class NotificationDto(
    val id: String,
    @SerialName("customer_id") val customerId: String,
    val type: String,
    val title: String,
    val body: String,
    @SerialName("related_type") val relatedType: String? = null,
    @SerialName("related_id") val relatedId: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("read_at") val readAt: String? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class RpcResultDto(
    val success: Boolean,
    val message: String? = null,
    @SerialName("request_id") val requestId: String? = null,
    @SerialName("customer_number_id") val customerNumberId: String? = null,
    val status: String? = null
)
