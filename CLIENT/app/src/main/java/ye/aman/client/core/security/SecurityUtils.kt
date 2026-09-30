package ye.aman.client.core.security

import java.security.MessageDigest

object SecurityUtils {
    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun maskPhoneNumber(number: String): String {
        if (number.length <= 4) return number
        val visiblePrefix = number.take(3)
        val visibleSuffix = number.takeLast(2)
        val maskedMiddle = "*".repeat(number.length - 5)
        return "$visiblePrefix$maskedMiddle$visibleSuffix"
    }
}
