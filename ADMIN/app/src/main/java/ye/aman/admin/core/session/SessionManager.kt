package ye.aman.admin.core.session

import android.content.Context
import android.content.SharedPreferences
import ye.aman.admin.core.common.Constants

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    fun saveSession(token: String, userId: String, email: String, name: String, role: String) {
        prefs.edit()
            .putString(Constants.KEY_AUTH_TOKEN, token)
            .putString(Constants.KEY_USER_ID, userId)
            .putString(Constants.KEY_USER_EMAIL, email)
            .putString(Constants.KEY_USER_NAME, name)
            .putString(Constants.KEY_USER_ROLE, role)
            .apply()
    }

    fun getToken(): String? = prefs.getString(Constants.KEY_AUTH_TOKEN, null)
    fun getUserId(): String? = prefs.getString(Constants.KEY_USER_ID, null)
    fun getUserEmail(): String? = prefs.getString(Constants.KEY_USER_EMAIL, null)
    fun getUserName(): String? = prefs.getString(Constants.KEY_USER_NAME, null)
    fun getUserRole(): String? = prefs.getString(Constants.KEY_USER_ROLE, null)

    fun isLoggedIn(): Boolean {
        val token = getToken()
        val role = getUserRole()
        return !token.isNullOrBlank() && (role == "admin" || role == "super_admin")
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
