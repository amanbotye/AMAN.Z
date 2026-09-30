package ye.aman.client.domain.model

data class CustomerNumber(
    val id: String,
    val customerId: String,
    val phoneNumberId: String,
    val normalizedNumber: String,
    val displayNumber: String,
    val providerId: String,
    val providerName: String,
    val providerCode: String,
    val customLabel: String?,
    val isProtected: Boolean,
    val protectionId: String?,
    val protectionStatus: String?,
    val protectionStartAt: String?,
    val protectionEndAt: String?,
    val daysRemaining: Int,
    val packageNameSnapshot: String?,
    val packagePriceSnapshot: Double?,
    val packageCurrencySnapshot: String?,
    val addedAt: String,
    val isActive: Boolean
)
