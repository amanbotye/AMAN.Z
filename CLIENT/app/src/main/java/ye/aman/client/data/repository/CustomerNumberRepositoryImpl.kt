package ye.aman.client.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ye.aman.client.core.result.AppResult
import ye.aman.client.data.local.dao.CustomerNumberDao
import ye.aman.client.data.mapper.Mappers.toDomain
import ye.aman.client.data.mapper.Mappers.toEntity
import ye.aman.client.data.remote.SupabaseRemoteDataSource
import ye.aman.client.domain.model.CustomerNumber
import ye.aman.client.domain.repository.CustomerNumberRepository

class CustomerNumberRepositoryImpl(
    private val numberDao: CustomerNumberDao,
    private val remoteDataSource: SupabaseRemoteDataSource
) : CustomerNumberRepository {

    override fun getCustomerNumbers(): Flow<List<CustomerNumber>> =
        numberDao.getNumbers().map { list -> list.map { it.toDomain() } }

    override suspend fun addCustomerNumber(phoneNumber: String, customLabel: String?): AppResult<CustomerNumber> {
        val result = remoteDataSource.addCustomerNumber(phoneNumber, customLabel)
        return when (result) {
            is AppResult.Success -> {
                val entity = result.data.toEntity()
                numberDao.insertNumber(entity)
                AppResult.Success(entity.toDomain())
            }
            is AppResult.Error -> result
        }
    }

    override suspend fun refreshNumbers(): AppResult<Unit> =
        AppResult.Success(Unit)
}
