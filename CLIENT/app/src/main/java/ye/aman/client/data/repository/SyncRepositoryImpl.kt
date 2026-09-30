package ye.aman.client.data.repository

import ye.aman.client.core.result.AppResult
import ye.aman.client.domain.repository.*

class SyncRepositoryImpl(
    private val numberRepository: CustomerNumberRepository,
    private val protectionRepository: ProtectionRepository,
    private val packageRepository: PackageRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val notificationRepository: NotificationRepository
) : SyncRepository {

    override suspend fun syncAll(): AppResult<Unit> {
        packageRepository.refreshPackages()
        packageRepository.getV1DefaultPackage()
        paymentMethodRepository.refreshPaymentMethods()
        numberRepository.refreshNumbers()
        protectionRepository.refreshProtections()
        protectionRepository.refreshRequests()
        notificationRepository.refreshNotifications()
        return AppResult.Success(Unit)
    }
}
