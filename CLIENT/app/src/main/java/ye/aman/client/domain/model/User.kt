package ye.aman.client.domain.model

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String?,
    val status: String,
    val userType: String,
    val createdAt: String
)
