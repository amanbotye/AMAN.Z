package ye.aman.admin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin_customers")
data class CustomerEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val phone: String,
    val email: String?,
    val status: String,
    val numbersCount: Int,
    val protectionsCount: Int,
    val createdAt: String
)

@Entity(tableName = "admin_phone_numbers")
data class AdminPhoneNumberEntity(
    @PrimaryKey val id: String,
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

@Entity(tableName = "admin_protection_requests")
data class AdminProtectionRequestEntity(
    @PrimaryKey val id: String,
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
    val requestType: String,
    val status: String,
    val paymentMethodId: String,
    val paymentMethodName: String,
    val transferNumber: String,
    val rejectionReason: String?,
    val createdAt: String
)

@Entity(tableName = "admin_protections")
data class AdminProtectionEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val customerName: String,
    val phoneNumberId: String,
    val phoneNumber: String,
    val providerName: String,
    val packageName: String,
    val status: String,
    val startedAt: String,
    val expiresAt: String,
    val autoRenew: Boolean,
    val remainingDays: Int
)

@Entity(tableName = "admin_payment_tasks")
data class PaymentTaskEntity(
    @PrimaryKey val id: String,
    val protectionId: String,
    val phoneNumber: String,
    val providerName: String,
    val customerName: String,
    val dueAt: String,
    val originalDueAt: String?,
    val status: String,
    val rescheduledAt: String?,
    val rescheduleReason: String?,
    val executionResult: String?,
    val executedAt: String?
)

@Entity(tableName = "admin_telecom_providers")
data class TelecomProviderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val numberLength: Int,
    val isActive: Boolean,
    val sortOrder: Int,
    val prefixesJson: String
)

@Entity(tableName = "admin_packages")
data class PackageEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val currency: String,
    val durationDays: Int,
    val isActive: Boolean,
    val isVisible: Boolean
)

@Entity(tableName = "admin_payment_methods")
data class PaymentMethodEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val accountNumber: String,
    val accountHolder: String,
    val isActive: Boolean
)
