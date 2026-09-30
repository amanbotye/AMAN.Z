package ye.aman.client.domain.usecase

import kotlinx.coroutines.flow.Flow
import ye.aman.client.core.result.AppResult
import ye.aman.client.core.validation.PhoneValidator
import ye.aman.client.core.error.AppError
import ye.aman.client.domain.model.CustomerNumber
import ye.aman.client.domain.repository.CustomerNumberRepository

class LoadCustomerNumbersUseCase(private val repository: CustomerNumberRepository) {
    operator fun invoke(): Flow<List<CustomerNumber>> =
        repository.getCustomerNumbers()
}

class AddCustomerNumberUseCase(private val repository: CustomerNumberRepository) {
    suspend operator fun invoke(phoneNumber: String, customLabel: String?): AppResult<CustomerNumber> {
        val validation = PhoneValidator.validateAndDetect(phoneNumber)
        if (!validation.isValid) {
            return AppResult.Error(AppError.ValidationError(validation.errorMessage ?: "رقم غير صالح"))
        }
        return repository.addCustomerNumber(validation.normalizedNumber, customLabel?.trim())
    }
}
