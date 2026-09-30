package ye.aman.client.core.session

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ye.aman.client.core.common.Constants

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentUserId = MutableStateFlow(prefs.getString(KEY_USER_ID, null))
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(_currentUserId.value != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    fun saveSession(userId: String, email: String, fullName: String) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_EMAIL, email)
            .putString(KEY_FULL_NAME, fullName)
            .apply()
        _currentUserId.value = userId
        _isAuthenticated.value = true
    }

    fun clearSession() {
        prefs.edit().clear().apply()
        _currentUserId.value = null
        _isAuthenticated.value = false
    }

    fun getSavedEmail(): String? = prefs.getString(KEY_EMAIL, null)
    fun getSavedFullName(): String? = prefs.getString(KEY_FULL_NAME, null)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_FULL_NAME = "full_name"
    }
}
