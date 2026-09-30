package ye.aman.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ye.aman.admin.di.ServiceLocator
import ye.aman.admin.presentation.auth.AdminLoginScreen
import ye.aman.admin.presentation.auth.AdminLoginViewModel
import ye.aman.admin.presentation.main.AdminMainShell
import ye.aman.admin.presentation.theme.AmanAdminTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AmanAdminTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val sessionManager = remember { ServiceLocator.sessionManager }
                    var isAuthenticated by remember { mutableStateOf(sessionManager.isLoggedIn()) }

                    if (isAuthenticated) {
                        AdminMainShell(
                            onLogout = {
                                sessionManager.clearSession()
                                isAuthenticated = false
                            }
                        )
                    } else {
                        val authViewModel = remember {
                            AdminLoginViewModel(
                                loginUseCase = ServiceLocator.adminLoginUseCase,
                                sessionManager = sessionManager
                            )
                        }
                        AdminLoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = {
                                isAuthenticated = true
                            }
                        )
                    }
                }
            }
        }
    }
}
