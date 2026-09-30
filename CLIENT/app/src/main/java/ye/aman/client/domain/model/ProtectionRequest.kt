package ye.aman.client.domain.model

data class ProtectionRequest(
    val id: String,
    val customerId: String,
    val phoneNumberId: String,
    val normalizedNumber: String,
    val providerName: String,
    val packageName: String,
    val amount: Double,
    val currency: String,
    val paymentMethodName: String,
    val paymentReference: String,
    val requestType: String, // new_protection / renewal
    val previousProtectionId: String?,
    val status: String, // pending / approved / rejected / conflict
    val rejectionReason: String?,
    val submittedAt: String,
    val reviewedAt: String?
)
