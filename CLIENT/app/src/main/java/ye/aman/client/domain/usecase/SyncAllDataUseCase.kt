package ye.aman.client.domain.usecase

import ye.aman.client.core.result.AppResult
import ye.aman.client.domain.repository.SyncRepository

class SyncAllDataUseCase(private val syncRepository: SyncRepository) {
    suspend operator fun invoke(): AppResult<Unit> =
        syncRepository.syncAll()
}
