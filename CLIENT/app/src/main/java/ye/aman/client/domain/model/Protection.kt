package ye.aman.client.domain.model

data class Protection(
    val id: String,
    val customerId: String,
    val phoneNumberId: String,
    val normalizedNumber: String,
    val providerName: String,
    val requestId: String,
    val packageId: String,
    val startAt: String,
    val endAt: String,
    val status: String,
    val daysRemaining: Int,
    val packageNameSnapshot: String,
    val packagePriceSnapshot: Double,
    val packageCurrencySnapshot: String,
    val isNearExpiry: Boolean = daysRemaining in 1..30,
    val isExpired: Boolean = daysRemaining <= 0
)
