package ye.aman.client.domain.model

data class ClientNotification(
    val id: String,
    val customerId: String,
    val type: String,
    val title: String,
    val body: String,
    val relatedType: String?,
    val relatedId: String?,
    val isRead: Boolean,
    val readAt: String?,
    val createdAt: String
)
