package ye.aman.client.domain.repository

import kotlinx.coroutines.flow.Flow
import ye.aman.client.core.result.AppResult
import ye.aman.client.domain.model.Package

interface PackageRepository {
    fun getAvailablePackages(): Flow<List<Package>>
    suspend fun getV1DefaultPackage(): AppResult<Package>
    suspend fun refreshPackages(): AppResult<Unit>
}
