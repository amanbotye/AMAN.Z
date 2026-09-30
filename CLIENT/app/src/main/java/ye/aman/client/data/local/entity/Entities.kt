package ye.aman.client.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_users")
data class UserEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String,
    val phone: String?,
    val status: String,
    val userType: String,
    val createdAt: String
)

@Entity(tableName = "local_customer_numbers")
data class CustomerNumberEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val phoneNumberId: String,
    val normalizedNumber: String,
    val displayNumber: String,
    val providerId: String,
    val providerName: String,
    val providerCode: String,
    val customLabel: String?,
    val isProtected: Boolean,
    val protectionId: String?,
    val protectionStatus: String?,
    val protectionStartAt: String?,
    val protectionEndAt: String?,
    val daysRemaining: Int,
    val packageNameSnapshot: String?,
    val packagePriceSnapshot: Double?,
    val packageCurrencySnapshot: String?,
    val addedAt: String,
    val isActive: Boolean
)

@Entity(tableName = "local_packages")
data class PackageEntity(
    @PrimaryKey val id: String,
    val providerId: String?,
    val name: String,
    val description: String?,
    val durationDays: Int,
    val price: Double,
    val currency: String,
    val isActive: Boolean,
    val isVisible: Boolean,
    val sortOrder: Int
)

@Entity(tableName = "local_payment_methods")
data class PaymentMethodEntity(
    @PrimaryKey val id: String,
    val type: String,
    val name: String,
    val recipientName: String,
    val accountNumber: String,
    val instructions: String?,
    val isActive: Boolean,
    val sortOrder: Int
)

@Entity(tableName = "local_protections")
data class ProtectionEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val phoneNumberId: String,
    val normalizedNumber: String,
    val providerName: String,
    val requestId: String,
    val packageId: String,
    val startAt: String,
    val endAt: String,
    val status: String,
    val daysRemaining: Int,
    val packageNameSnapshot: String,
    val packagePriceSnapshot: Double,
    val packageCurrencySnapshot: String
)

@Entity(tableName = "local_protection_requests")
data class ProtectionRequestEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val phoneNumberId: String,
    val normalizedNumber: String,
    val providerName: String,
    val packageName: String,
    val amount: Double,
    val currency: String,
    val paymentMethodName: String,
    val paymentReference: String,
    val requestType: String,
    val previousProtectionId: String?,
    val status: String,
    val rejectionReason: String?,
    val submittedAt: String,
    val reviewedAt: String?
)

@Entity(tableName = "local_notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val type: String,
    val title: String,
    val body: String,
    val relatedType: String?,
    val relatedId: String?,
    val isRead: Boolean,
    val readAt: String?,
    val createdAt: String
)

@Entity(tableName = "local_sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey val entityName: String,
    val lastSyncAt: Long,
    val syncStatus: String
)
