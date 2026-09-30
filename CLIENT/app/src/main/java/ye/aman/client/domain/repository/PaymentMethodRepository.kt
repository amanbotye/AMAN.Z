package ye.aman.client.domain.repository

import kotlinx.coroutines.flow.Flow
import ye.aman.client.core.result.AppResult
import ye.aman.client.domain.model.PaymentMethod

interface PaymentMethodRepository {
    fun getActivePaymentMethods(): Flow<List<PaymentMethod>>
    suspend fun refreshPaymentMethods(): AppResult<Unit>
}
