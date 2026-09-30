package ye.aman.client.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ye.aman.client.domain.model.ClientNotification
import ye.aman.client.domain.usecase.LoadNotificationsUseCase
import ye.aman.client.domain.usecase.MarkAllNotificationsReadUseCase
import ye.aman.client.domain.usecase.MarkNotificationReadUseCase

data class NotificationsUiState(
    val notifications: List<ClientNotification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false
)

class NotificationsViewModel(
    private val loadNotificationsUseCase: LoadNotificationsUseCase,
    private val markNotificationReadUseCase: MarkNotificationReadUseCase,
    private val markAllNotificationsReadUseCase: MarkAllNotificationsReadUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadNotificationsUseCase().collect { list ->
                _uiState.value = _uiState.value.copy(
                    notifications = list,
                    unreadCount = list.count { !it.isRead }
                )
            }
        }
    }

    fun markRead(id: String) {
        viewModelScope.launch {
            markNotificationReadUseCase(id)
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            markAllNotificationsReadUseCase()
        }
    }
}
