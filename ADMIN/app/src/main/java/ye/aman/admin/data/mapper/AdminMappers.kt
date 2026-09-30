package ye.aman.admin.data.mapper

import ye.aman.admin.data.local.entity.*
import ye.aman.admin.data.remote.SupabaseAdminRemoteDataSource
import ye.aman.admin.data.remote.dto.*
import ye.aman.admin.domain.model.*

object AdminMappers {

    fun CustomerDto.toEntity() = CustomerEntity(
        id = id,
        fullName = fullName,
        phone = phone,
        email = email,
        status = status,
        numbersCount = 0,
        protectionsCount = 0,
        createdAt = createdAt
    )

    fun CustomerEntity.toDomain() = CustomerRecord(
        id = id,
        fullName = fullName,
        phone = phone,
        email = email,
        status = status,
        numbersCount = numbersCount,
        protectionsCount = protectionsCount,
        createdAt = createdAt
    )

    fun AdminPhoneNumberDto.toEntity() = AdminPhoneNumberEntity(
        id = id,
        phoneNumber = phoneNumber,
        providerId = providerId,
        providerName = providerName ?: "مشغل اتصالات",
        providerCode = providerCode ?: "",
        status = status,
        customerId = customerId,
        customerName = customerName,
        isProtected = isProtected,
        protectionExpiresAt = protectionExpiresAt
    )

    fun AdminPhoneNumberEntity.toDomain() = AdminPhoneNumber(
        id = id,
        phoneNumber = phoneNumber,
        providerId = providerId,
        providerName = providerName,
        providerCode = providerCode,
        status = status,
        customerId = customerId,
        customerName = customerName,
        isProtected = isProtected,
        protectionExpiresAt = protectionExpiresAt
    )

    fun AdminProtectionRequestDto.toEntity() = AdminProtectionRequestEntity(
        id = id,
        requestNumber = requestNumber,
        customerId = customerId,
        customerName = customerName ?: "عميل",
        customerPhone = customerPhone ?: "",
        phoneNumberId = phoneNumberId,
        phoneNumber = phoneNumber ?: "",
        providerName = providerName ?: "",
        packageId = packageId,
        packageName = packageName ?: "باقة سنوية",
        packagePrice = packagePrice,
        requestType = requestType,
        status = status,
        paymentMethodId = paymentMethodId,
        paymentMethodName = paymentMethodName ?: "حوالة",
        transferNumber = transferNumber,
        rejectionReason = rejectionReason,
        createdAt = createdAt
    )

    fun AdminProtectionRequestEntity.toDomain() = AdminProtectionRequest(
        id = id,
        requestNumber = requestNumber,
        customerId = customerId,
        customerName = customerName,
        customerPhone = customerPhone,
        phoneNumberId = phoneNumberId,
        phoneNumber = phoneNumber,
        providerName = providerName,
        packageId = packageId,
        packageName = packageName,
        packagePrice = packagePrice,
        requestType = requestType,
        status = status,
        paymentMethodId = paymentMethodId,
        paymentMethodName = paymentMethodName,
        transferNumber = transferNumber,
        rejectionReason = rejectionReason,
        createdAt = createdAt
    )

    fun AdminProtectionDto.toEntity() = AdminProtectionEntity(
        id = id,
        customerId = customerId,
        customerName = customerName ?: "عميل",
        phoneNumberId = phoneNumberId,
        phoneNumber = phoneNumber ?: "",
        providerName = providerName ?: "",
        packageName = packageName ?: "باقة سنوية",
        status = status,
        startedAt = startedAt,
        expiresAt = expiresAt,
        autoRenew = autoRenew,
        remainingDays = 365
    )

    fun AdminProtectionEntity.toDomain() = AdminProtection(
        id = id,
        customerId = customerId,
        customerName = customerName,
        phoneNumberId = phoneNumberId,
        phoneNumber = phoneNumber,
        providerName = providerName,
        packageName = packageName,
        status = status,
        startedAt = startedAt,
        expiresAt = expiresAt,
        autoRenew = autoRenew,
        remainingDays = remainingDays
    )

    fun PaymentTaskDto.toEntity() = PaymentTaskEntity(
        id = id,
        protectionId = protectionId,
        phoneNumber = phoneNumber ?: "",
        providerName = providerName ?: "",
        customerName = customerName ?: "",
        dueAt = dueAt,
        originalDueAt = originalDueAt,
        status = status,
        rescheduledAt = rescheduledAt,
        rescheduleReason = rescheduleReason,
        executionResult = executionResult,
        executedAt = executedAt
    )

    fun PaymentTaskEntity.toDomain() = PaymentTask(
        id = id,
        protectionId = protectionId,
        phoneNumber = phoneNumber,
        providerName = providerName,
        customerName = customerName,
        dueAt = dueAt,
        originalDueAt = originalDueAt,
        status = status,
        rescheduledAt = rescheduledAt,
        rescheduleReason = rescheduleReason,
        executionResult = executionResult,
        executedAt = executedAt
    )

    fun TelecomProviderDto.toDomain() = TelecomProvider(
        id = id,
        name = name,
        code = code,
        numberLength = numberLength,
        isActive = isActive,
        sortOrder = sortOrder,
        prefixes = emptyList()
    )

    fun PackageDto.toDomain() = Package(
        id = id,
        name = name,
        price = price,
        currency = currency,
        durationDays = durationDays,
        isActive = isActive,
        isVisible = isVisible
    )

    fun PaymentMethodDto.toDomain() = PaymentMethod(
        id = id,
        name = name,
        type = type,
        accountNumber = accountNumber,
        accountHolder = accountHolder,
        isActive = isActive
    )
}
