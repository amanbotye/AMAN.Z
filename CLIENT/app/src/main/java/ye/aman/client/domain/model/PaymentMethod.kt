package ye.aman.client.domain.model

data class PaymentMethod(
    val id: String,
    val type: String,
    val name: String,
    val recipientName: String,
    val accountNumber: String,
    val instructions: String?,
    val isActive: Boolean,
    val sortOrder: Int
)
