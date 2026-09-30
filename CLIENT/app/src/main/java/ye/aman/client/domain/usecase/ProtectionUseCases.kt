package ye.aman.client.domain.usecase

import kotlinx.coroutines.flow.Flow
import ye.aman.client.core.result.AppResult
import ye.aman.client.core.error.AppError
import ye.aman.client.domain.model.Protection
import ye.aman.client.domain.model.ProtectionRequest
import ye.aman.client.domain.repository.ProtectionRepository

class LoadProtectionsUseCase(private val repository: ProtectionRepository) {
    operator fun invoke(): Flow<List<Protection>> =
        repository.getProtections()
}

class LoadProtectionRequestsUseCase(private val repository: ProtectionRepository) {
    operator fun invoke(): Flow<List<ProtectionRequest>> =
        repository.getProtectionRequests()
}

class CreateProtectionRequestUseCase(private val repository: ProtectionRepository) {
    suspend operator fun invoke(
        customerNumberId: String,
        packageId: String,
        paymentMethodId: String,
        paymentReference: String
    ): AppResult<ProtectionRequest> {
        if (paymentReference.isBlank()) {
            return AppResult.Error(AppError.ValidationError("مرجع التحويل مطلوب لإثبات الدفع"))
        }
        return repository.createProtectionRequest(
            customerNumberId = customerNumberId,
            packageId = packageId,
            paymentMethodId = paymentMethodId,
            paymentReference = paymentReference.trim()
        )
    }
}

class CreateRenewalRequestUseCase(private val repository: ProtectionRepository) {
    suspend operator fun invoke(
        protectionId: String,
        packageId: String,
        paymentMethodId: String,
        paymentReference: String
    ): AppResult<ProtectionRequest> {
        if (paymentReference.isBlank()) {
            return AppResult.Error(AppError.ValidationError("مرجع التحويل مطلوب لإثبات الدفع"))
        }
        return repository.createRenewalRequest(
            protectionId = protectionId,
            packageId = packageId,
            paymentMethodId = paymentMethodId,
            paymentReference = paymentReference.trim()
        )
    }
}
