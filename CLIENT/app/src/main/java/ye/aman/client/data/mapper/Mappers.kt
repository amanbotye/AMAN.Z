package ye.aman.client.data.mapper

import ye.aman.client.data.local.entity.*
import ye.aman.client.data.remote.dto.*
import ye.aman.client.domain.model.*

object Mappers {

    // User Mappings
    fun UserDto.toEntity() = UserEntity(id, fullName, email, phone, status, userType, createdAt)
    fun UserEntity.toDomain() = User(id, fullName, email, phone, status, userType, createdAt)
    fun UserDto.toDomain() = User(id, fullName, email, phone, status, userType, createdAt)

    // Customer Number Mappings
    fun CustomerNumberDto.toEntity() = CustomerNumberEntity(
        id = customerNumberId,
        customerId = customerId,
        phoneNumberId = phoneNumberId,
        normalizedNumber = normalizedNumber,
        displayNumber = displayNumber,
        providerId = providerId,
        providerName = providerName,
        providerCode = providerCode,
        customLabel = customLabel,
        isProtected = isProtected,
        protectionId = protectionId,
        protectionStatus = protectionStatus,
        protectionStartAt = protectionStartAt,
        protectionEndAt = protectionEndAt,
        daysRemaining = daysRemaining,
        packageNameSnapshot = packageNameSnapshot,
        packagePriceSnapshot = packagePriceSnapshot,
        packageCurrencySnapshot = packageCurrencySnapshot,
        addedAt = addedAt,
        isActive = customerNumberActive
    )

    fun CustomerNumberEntity.toDomain() = CustomerNumber(
        id = id,
        customerId = customerId,
        phoneNumberId = phoneNumberId,
        normalizedNumber = normalizedNumber,
        displayNumber = displayNumber,
        providerId = providerId,
        providerName = providerName,
        providerCode = providerCode,
        customLabel = customLabel,
        isProtected = isProtected,
        protectionId = protectionId,
        protectionStatus = protectionStatus,
        protectionStartAt = protectionStartAt,
        protectionEndAt = protectionEndAt,
        daysRemaining = daysRemaining,
        packageNameSnapshot = packageNameSnapshot,
        packagePriceSnapshot = packagePriceSnapshot,
        packageCurrencySnapshot = packageCurrencySnapshot,
        addedAt = addedAt,
        isActive = isActive
    )

    // Package Mappings
    fun PackageDto.toEntity() = PackageEntity(id, providerId, name, description, durationDays, price, currency, isActive, isVisible, sortOrder)
    fun PackageEntity.toDomain() = Package(id, providerId, name, description, durationDays, price, currency, isActive, isVisible, sortOrder)

    // Payment Method Mappings
    fun PaymentMethodDto.toEntity() = PaymentMethodEntity(id, type, name, recipientName, accountNumber, instructions, isActive, sortOrder)
    fun PaymentMethodEntity.toDomain() = PaymentMethod(id, type, name, recipientName, accountNumber, instructions, isActive, sortOrder)

    // Protection Mappings
    fun ProtectionDto.toEntity(daysRemaining: Int) = ProtectionEntity(
        id = id,
        customerId = customerId,
        phoneNumberId = phoneNumberId,
        normalizedNumber = "",
        providerName = providerNameSnapshot,
        requestId = requestId,
        packageId = packageId,
        startAt = startAt,
        endAt = endAt,
        status = status,
        daysRemaining = daysRemaining,
        packageNameSnapshot = packageNameSnapshot,
        packagePriceSnapshot = packagePriceSnapshot,
        packageCurrencySnapshot = packageCurrencySnapshot
    )

    fun ProtectionEntity.toDomain() = Protection(
        id = id,
        customerId = customerId,
        phoneNumberId = phoneNumberId,
        normalizedNumber = normalizedNumber,
        providerName = providerName,
        requestId = requestId,
        packageId = packageId,
        startAt = startAt,
        endAt = endAt,
        status = status,
        daysRemaining = daysRemaining,
        packageNameSnapshot = packageNameSnapshot,
        packagePriceSnapshot = packagePriceSnapshot,
        packageCurrencySnapshot = packageCurrencySnapshot
    )

    // Protection Request Mappings
    fun ProtectionRequestDto.toEntity() = ProtectionRequestEntity(
        id = id,
        customerId = customerId,
        phoneNumberId = phoneNumberId,
        normalizedNumber = "",
        providerName = providerName,
        packageName = packageName,
        amount = amount,
        currency = currency,
        paymentMethodName = paymentMethodName,
        paymentReference = paymentReference,
        requestType = requestType,
        previousProtectionId = previousProtectionId,
        status = status,
        rejectionReason = rejectionReason,
        submittedAt = submittedAt,
        reviewedAt = reviewedAt
    )

    fun ProtectionRequestEntity.toDomain() = ProtectionRequest(
        id = id,
        customerId = customerId,
        phoneNumberId = phoneNumberId,
        normalizedNumber = normalizedNumber,
        providerName = providerName,
        packageName = packageName,
        amount = amount,
        currency = currency,
        paymentMethodName = paymentMethodName,
        paymentReference = paymentReference,
        requestType = requestType,
        previousProtectionId = previousProtectionId,
        status = status,
        rejectionReason = rejectionReason,
        submittedAt = submittedAt,
        reviewedAt = reviewedAt
    )

    // Notification Mappings
    fun NotificationDto.toEntity() = NotificationEntity(id, customerId, type, title, body, relatedType, relatedId, isRead, readAt, createdAt)
    fun NotificationEntity.toDomain() = ClientNotification(id, customerId, type, title, body, relatedType, relatedId, isRead, readAt, createdAt)
}
