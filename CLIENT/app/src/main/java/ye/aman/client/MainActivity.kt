package ye.aman.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import ye.aman.client.di.ServiceLocator
import ye.aman.client.presentation.auth.AuthViewModel
import ye.aman.client.presentation.auth.LoginScreen
import ye.aman.client.presentation.auth.RegisterScreen
import ye.aman.client.presentation.home.HomeViewModel
import ye.aman.client.presentation.main.MainShell
import ye.aman.client.presentation.notifications.NotificationsViewModel
import ye.aman.client.presentation.numbers.NumbersViewModel
import ye.aman.client.presentation.protection.ProtectionViewModel
import ye.aman.client.presentation.settings.SettingsViewModel
import ye.aman.client.presentation.theme.AmanClientTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authViewModel = AuthViewModel(
            ServiceLocator.loginUseCase,
            ServiceLocator.registerUseCase,
            ServiceLocator.resetPasswordUseCase
        )

        val homeViewModel = HomeViewModel(
            ServiceLocator.loadNumbersUseCase,
            ServiceLocator.loadProtectionsUseCase,
            ServiceLocator.syncAllDataUseCase
        )

        val numbersViewModel = NumbersViewModel(
            ServiceLocator.loadNumbersUseCase,
            ServiceLocator.addNumberUseCase
        )

        val protectionViewModel = ProtectionViewModel(
            ServiceLocator.packageRepository,
            ServiceLocator.paymentMethodRepository,
            ServiceLocator.createProtectionRequestUseCase,
            ServiceLocator.createRenewalRequestUseCase
        )

        val notificationsViewModel = NotificationsViewModel(
            ServiceLocator.loadNotificationsUseCase,
            ServiceLocator.markNotificationReadUseCase,
            ServiceLocator.markAllNotificationsReadUseCase
        )

        val settingsViewModel = SettingsViewModel(
            ServiceLocator.authRepository,
            ServiceLocator.updateAccountUseCase,
            ServiceLocator.changePasswordUseCase,
            ServiceLocator.logoutUseCase
        )

        setContent {
            AmanClientTheme {
                var currentScreen by remember { mutableStateOf("login") }

                when (currentScreen) {
                    "login" -> LoginScreen(
                        viewModel = authViewModel,
                        onNavigateToRegister = { currentScreen = "register" },
                        onLoginSuccess = { currentScreen = "main" }
                    )
                    "register" -> RegisterScreen(
                        viewModel = authViewModel,
                        onNavigateToLogin = { currentScreen = "login" },
                        onRegisterSuccess = { currentScreen = "main" }
                    )
                    "main" -> MainShell(
                        homeViewModel = homeViewModel,
                        numbersViewModel = numbersViewModel,
                        protectionViewModel = protectionViewModel,
                        notificationsViewModel = notificationsViewModel,
                        settingsViewModel = settingsViewModel,
                        onLogout = { currentScreen = "login" }
                    )
                }
            }
        }
    }
}
