package ye.aman.client.domain.model

data class TelecomProvider(
    val id: String,
    val name: String,
    val code: String,
    val numberLength: Int,
    val isActive: Boolean,
    val sortOrder: Int
)
