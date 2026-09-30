package ye.aman.client.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ye.aman.client.core.result.AppResult
import ye.aman.client.data.local.dao.ProtectionDao
import ye.aman.client.data.local.dao.ProtectionRequestDao
import ye.aman.client.data.mapper.Mappers.toDomain
import ye.aman.client.data.mapper.Mappers.toEntity
import ye.aman.client.data.remote.SupabaseRemoteDataSource
import ye.aman.client.domain.model.Protection
import ye.aman.client.domain.model.ProtectionRequest
import ye.aman.client.domain.repository.ProtectionRepository

class ProtectionRepositoryImpl(
    private val protectionDao: ProtectionDao,
    private val requestDao: ProtectionRequestDao,
    private val remoteDataSource: SupabaseRemoteDataSource
) : ProtectionRepository {

    override fun getProtections(): Flow<List<Protection>> =
        protectionDao.getProtections().map { list -> list.map { it.toDomain() } }

    override fun getProtectionRequests(): Flow<List<ProtectionRequest>> =
        requestDao.getRequests().map { list -> list.map { it.toDomain() } }

    override suspend fun createProtectionRequest(
        customerNumberId: String,
        packageId: String,
        paymentMethodId: String,
        paymentReference: String
    ): AppResult<ProtectionRequest> {
        val result = remoteDataSource.createProtectionRequest(
            customerNumberId, packageId, paymentMethodId, paymentReference
        )
        return when (result) {
            is AppResult.Success -> {
                val entity = result.data.toEntity()
                requestDao.insertRequest(entity)
                AppResult.Success(entity.toDomain())
            }
            is AppResult.Error -> result
        }
    }

    override suspend fun createRenewalRequest(
        protectionId: String,
        packageId: String,
        paymentMethodId: String,
        paymentReference: String
    ): AppResult<ProtectionRequest> {
        val result = remoteDataSource.createRenewalRequest(
            protectionId, packageId, paymentMethodId, paymentReference
        )
        return when (result) {
            is AppResult.Success -> {
                val entity = result.data.toEntity()
                requestDao.insertRequest(entity)
                AppResult.Success(entity.toDomain())
            }
            is AppResult.Error -> result
        }
    }

    override suspend fun refreshProtections(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun refreshRequests(): AppResult<Unit> = AppResult.Success(Unit)
}
