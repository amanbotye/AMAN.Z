package ye.aman.client.domain.model

data class Package(
    val id: String,
    val providerId: String?,
    val name: String,
    val description: String?,
    val durationDays: Int,
    val price: Double,
    val currency: String,
    val isActive: Boolean,
    val isVisible: Boolean,
    val sortOrder: Int
)
