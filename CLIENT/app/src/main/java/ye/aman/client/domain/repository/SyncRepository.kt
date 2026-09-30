package ye.aman.client.domain.repository

import ye.aman.client.core.result.AppResult

interface SyncRepository {
    suspend fun syncAll(): AppResult<Unit>
}
