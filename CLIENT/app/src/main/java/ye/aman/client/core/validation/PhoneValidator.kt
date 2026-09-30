package ye.aman.client.core.validation

object PhoneValidator {
    data class ValidationResult(
        val isValid: Boolean,
        val normalizedNumber: String,
        val detectedProviderCode: String?,
        val errorMessage: String? = null
    )

    private val prefixMap = mapOf(
        "77" to "YOU",     // Yemen Mobile / YOU
        "78" to "YOU",     // Yemen Mobile
        "73" to "MTN",     // MTN / Spacetel
        "71" to "SABAFON", // Sabafon
        "70" to "Y"        // Y Telecom
    )

    fun validateAndDetect(rawNumber: String): ValidationResult {
        val cleaned = rawNumber.filter { it.isDigit() }

        if (cleaned.isBlank()) {
            return ValidationResult(false, "", null, "رقم الهاتف مطلوب")
        }

        val normalized = when {
            cleaned.startsWith("967") -> cleaned.removePrefix("967")
            cleaned.startsWith("00967") -> cleaned.removePrefix("00967")
            cleaned.startsWith("0") && cleaned.length == 10 -> cleaned.removePrefix("0")
            else -> cleaned
        }

        if (normalized.length != 9) {
            return ValidationResult(
                isValid = false,
                normalizedNumber = normalized,
                detectedProviderCode = null,
                errorMessage = "طول الرقم غير صحيح (المطلوب 9 أرقام، المدخل: " + normalized.length + ")"
            )
        }

        val prefix2 = normalized.take(2)
        val providerCode = prefixMap[prefix2]

        if (providerCode == null) {
            return ValidationResult(
                isValid = false,
                normalizedNumber = normalized,
                detectedProviderCode = null,
                errorMessage = "بادئة الرقم غير معتمدة لدى أي شركة اتصالات ($prefix2)"
            )
        }

        return ValidationResult(
            isValid = true,
            normalizedNumber = normalized,
            detectedProviderCode = providerCode,
            errorMessage = null
        )
    }
}
