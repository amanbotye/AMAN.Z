package ye.aman.client.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ye.aman.client.core.common.Constants
import ye.aman.client.core.result.AppResult
import ye.aman.client.data.local.dao.PackageDao
import ye.aman.client.data.local.entity.PackageEntity
import ye.aman.client.data.mapper.Mappers.toDomain
import ye.aman.client.domain.model.Package
import ye.aman.client.domain.repository.PackageRepository

class PackageRepositoryImpl(private val packageDao: PackageDao) : PackageRepository {

    override fun getAvailablePackages(): Flow<List<Package>> =
        packageDao.getPackages().map { list -> list.map { it.toDomain() } }

    override suspend fun getV1DefaultPackage(): AppResult<Package> {
        val v1Package = Package(
            id = "pkg_v1_default",
            providerId = null,
            name = "باقة أمان السنوية",
            description = "حماية رقم الهاتف من إعادة البيع أو السحب لمدة 365 يوماً",
            durationDays = Constants.V1_PACKAGE_DURATION_DAYS,
            price = Constants.V1_PACKAGE_PRICE,
            currency = Constants.V1_CURRENCY,
            isActive = true,
            isVisible = true,
            sortOrder = 1
        )
        packageDao.insertPackages(listOf(
            PackageEntity(
                id = v1Package.id,
                providerId = v1Package.providerId,
                name = v1Package.name,
                description = v1Package.description,
                durationDays = v1Package.durationDays,
                price = v1Package.price,
                currency = v1Package.currency,
                isActive = v1Package.isActive,
                isVisible = v1Package.isVisible,
                sortOrder = v1Package.sortOrder
            )
        ))
        return AppResult.Success(v1Package)
    }

    override suspend fun refreshPackages(): AppResult<Unit> = AppResult.Success(Unit)
}
