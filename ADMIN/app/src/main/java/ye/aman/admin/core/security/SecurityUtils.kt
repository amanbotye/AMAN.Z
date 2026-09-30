package ye.aman.admin.core.security

object SecurityUtils {
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    fun sanitizeInput(input: String): String {
        return input.trim()
    }
}
