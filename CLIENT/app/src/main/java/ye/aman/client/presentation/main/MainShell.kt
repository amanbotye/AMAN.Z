package ye.aman.client.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ye.aman.client.domain.model.CustomerNumber
import ye.aman.client.domain.model.Protection
import ye.aman.client.presentation.home.HomeScreen
import ye.aman.client.presentation.home.HomeViewModel
import ye.aman.client.presentation.notifications.NotificationsScreen
import ye.aman.client.presentation.notifications.NotificationsViewModel
import ye.aman.client.presentation.numbers.NumbersScreen
import ye.aman.client.presentation.numbers.NumbersViewModel
import ye.aman.client.presentation.protection.ProtectionDetailsDialog
import ye.aman.client.presentation.protection.ProtectionViewModel
import ye.aman.client.presentation.protection.RenewalDialog
import ye.aman.client.presentation.protection.RequestProtectionDialog
import ye.aman.client.presentation.settings.SettingsScreen
import ye.aman.client.presentation.settings.SettingsViewModel
import ye.aman.client.presentation.theme.NavyPrimary

@Composable
fun MainShell(
    homeViewModel: HomeViewModel,
    numbersViewModel: NumbersViewModel,
    protectionViewModel: ProtectionViewModel,
    notificationsViewModel: NotificationsViewModel,
    settingsViewModel: SettingsViewModel,
    onLogout: () -> Unit
) {
    var currentTab by remember { mutableStateOf(0) }
    var selectedCustomerNumberForProtection by remember { mutableStateOf<CustomerNumber?>(null) }
    var selectedProtectionForDetails by remember { mutableStateOf<Protection?>(null) }
    var selectedProtectionForRenewal by remember { mutableStateOf<Protection?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                    label = { Text("الرئيسية") }
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Phone, contentDescription = "أرقامي") },
                    label = { Text("أرقامي") }
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "الإشعارات") },
                    label = { Text("الإشعارات") }
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "الإعدادات") },
                    label = { Text("الإعدادات") }
                )
            }
        }
    ) { padding ->
        when (currentTab) {
            0 -> HomeScreen(
                viewModel = homeViewModel,
                onNavigateToNumbers = { currentTab = 1 },
                onOpenProtectionDetails = { protId ->
                    // Find and open protection details
                }
            )
            1 -> NumbersScreen(
                viewModel = numbersViewModel,
                onRequestProtection = { num -> selectedCustomerNumberForProtection = num },
                onOpenProtectionDetails = { protId -> }
            )
            2 -> NotificationsScreen(viewModel = notificationsViewModel)
            3 -> SettingsScreen(
                viewModel = settingsViewModel,
                onLoggedOut = onLogout
            )
        }
    }

    selectedCustomerNumberForProtection?.let { num ->
        RequestProtectionDialog(
            customerNumber = num,
            viewModel = protectionViewModel,
            onDismiss = { selectedCustomerNumberForProtection = null }
        )
    }

    selectedProtectionForDetails?.let { prot ->
        ProtectionDetailsDialog(
            protection = prot,
            onDismiss = { selectedProtectionForDetails = null },
            onOpenRenewal = {
                selectedProtectionForDetails = null
                selectedProtectionForRenewal = prot
            }
        )
    }

    selectedProtectionForRenewal?.let { prot ->
        RenewalDialog(
            protection = prot,
            viewModel = protectionViewModel,
            onDismiss = { selectedProtectionForRenewal = null }
        )
    }
}
