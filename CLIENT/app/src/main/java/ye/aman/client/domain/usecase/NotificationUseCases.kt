package ye.aman.client.domain.usecase

import kotlinx.coroutines.flow.Flow
import ye.aman.client.core.result.AppResult
import ye.aman.client.domain.model.ClientNotification
import ye.aman.client.domain.repository.NotificationRepository

class LoadNotificationsUseCase(private val repository: NotificationRepository) {
    operator fun invoke(): Flow<List<ClientNotification>> =
        repository.getNotifications()
}

class MarkNotificationReadUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(notificationId: String): AppResult<Unit> =
        repository.markAsRead(notificationId)
}

class MarkAllNotificationsReadUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(): AppResult<Unit> =
        repository.markAllAsRead()
}
