package ye.aman.client.domain.repository

import kotlinx.coroutines.flow.Flow
import ye.aman.client.core.result.AppResult
import ye.aman.client.domain.model.Protection
import ye.aman.client.domain.model.ProtectionRequest

interface ProtectionRepository {
    fun getProtections(): Flow<List<Protection>>
    fun getProtectionRequests(): Flow<List<ProtectionRequest>>

    suspend fun createProtectionRequest(
        customerNumberId: String,
        packageId: String,
        paymentMethodId: String,
        paymentReference: String
    ): AppResult<ProtectionRequest>

    suspend fun createRenewalRequest(
        protectionId: String,
        packageId: String,
        paymentMethodId: String,
        paymentReference: String
    ): AppResult<ProtectionRequest>

    suspend fun refreshProtections(): AppResult<Unit>
    suspend fun refreshRequests(): AppResult<Unit>
}
