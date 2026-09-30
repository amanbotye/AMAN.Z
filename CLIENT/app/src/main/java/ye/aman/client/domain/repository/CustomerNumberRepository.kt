package ye.aman.client.domain.repository

import kotlinx.coroutines.flow.Flow
import ye.aman.client.core.result.AppResult
import ye.aman.client.domain.model.CustomerNumber

interface CustomerNumberRepository {
    fun getCustomerNumbers(): Flow<List<CustomerNumber>>
    suspend fun addCustomerNumber(phoneNumber: String, customLabel: String?): AppResult<CustomerNumber>
    suspend fun refreshNumbers(): AppResult<Unit>
}
