package ye.aman.client.domain.repository

import kotlinx.coroutines.flow.Flow
import ye.aman.client.core.result.AppResult
import ye.aman.client.domain.model.ClientNotification

interface NotificationRepository {
    fun getNotifications(): Flow<List<ClientNotification>>
    fun getUnreadCount(): Flow<Int>
    suspend fun markAsRead(notificationId: String): AppResult<Unit>
    suspend fun markAllAsRead(): AppResult<Unit>
    suspend fun refreshNotifications(): AppResult<Unit>
}
