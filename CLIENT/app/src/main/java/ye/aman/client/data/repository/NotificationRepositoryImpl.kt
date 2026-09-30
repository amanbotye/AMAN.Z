package ye.aman.client.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ye.aman.client.core.result.AppResult
import ye.aman.client.data.local.dao.NotificationDao
import ye.aman.client.data.mapper.Mappers.toDomain
import ye.aman.client.data.remote.SupabaseRemoteDataSource
import ye.aman.client.domain.model.ClientNotification
import ye.aman.client.domain.repository.NotificationRepository

class NotificationRepositoryImpl(
    private val notificationDao: NotificationDao,
    private val remoteDataSource: SupabaseRemoteDataSource
) : NotificationRepository {

    override fun getNotifications(): Flow<List<ClientNotification>> =
        notificationDao.getNotifications().map { list -> list.map { it.toDomain() } }

    override fun getUnreadCount(): Flow<Int> =
        notificationDao.getUnreadCount()

    override suspend fun markAsRead(notificationId: String): AppResult<Unit> {
        val now = java.time.Instant.now().toString()
        notificationDao.markAsRead(notificationId, now)
        return remoteDataSource.markNotificationRead(notificationId)
    }

    override suspend fun markAllAsRead(): AppResult<Unit> {
        val now = java.time.Instant.now().toString()
        notificationDao.markAllAsRead(now)
        return remoteDataSource.markAllNotificationsRead()
    }

    override suspend fun refreshNotifications(): AppResult<Unit> = AppResult.Success(Unit)
}
