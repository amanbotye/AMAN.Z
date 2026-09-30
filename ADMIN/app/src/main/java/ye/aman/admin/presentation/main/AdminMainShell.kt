package ye.aman.admin.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ye.aman.admin.di.ServiceLocator
import ye.aman.admin.presentation.customers.CustomersScreen
import ye.aman.admin.presentation.customers.CustomersViewModel
import ye.aman.admin.presentation.dashboard.AdminDashboardScreen
import ye.aman.admin.presentation.dashboard.AdminDashboardViewModel
import ye.aman.admin.presentation.navigation.Screen
import ye.aman.admin.presentation.numbers.AdminNumbersScreen
import ye.aman.admin.presentation.numbers.AdminNumbersViewModel
import ye.aman.admin.presentation.operations.OperationsScreen
import ye.aman.admin.presentation.operations.OperationsViewModel
import ye.aman.admin.presentation.settings.AdminSettingsScreen
import ye.aman.admin.presentation.settings.AdminSettingsViewModel
import ye.aman.admin.presentation.theme.*

@Composable
fun AdminMainShell(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    val dashboardViewModel = remember {
        AdminDashboardViewModel(ServiceLocator.getDashboardSummaryUseCase)
    }
    val customersViewModel = remember {
        CustomersViewModel(ServiceLocator.getCustomersUseCase, ServiceLocator.updateCustomerStatusUseCase)
    }
    val numbersViewModel = remember {
        AdminNumbersViewModel(ServiceLocator.getAdminNumbersUseCase)
    }
    val operationsViewModel = remember {
        OperationsViewModel(ServiceLocator.operationsUseCases)
    }
    val settingsViewModel = remember {
        AdminSettingsViewModel(ServiceLocator.settingsUseCases)
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Emerald500, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Slate950,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text("أمان | الإدارة", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                        Text(currentScreen.title, color = Emerald500, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }

                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "تسجيل الخروج",
                        tint = Rose500
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Slate900,
                contentColor = TextPrimary
            ) {
                listOf(
                    NavigationItem(Screen.Dashboard, "الرئيسية", Icons.Default.Dashboard),
                    NavigationItem(Screen.Customers, "العملاء", Icons.Default.People),
                    NavigationItem(Screen.Numbers, "الأرقام", Icons.Default.Phone),
                    NavigationItem(Screen.Operations, "التشغيل", Icons.Default.Build),
                    NavigationItem(Screen.Settings, "الإعدادات", Icons.Default.Settings)
                ).forEach { item ->
                    NavigationBarItem(
                        selected = currentScreen == item.screen,
                        onClick = { currentScreen = item.screen },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Slate950,
                            selectedTextColor = Emerald500,
                            indicatorColor = Emerald500,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                is Screen.Dashboard -> AdminDashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToOperations = { currentScreen = Screen.Operations },
                    onNavigateToCustomers = { currentScreen = Screen.Customers }
                )
                is Screen.Customers -> CustomersScreen(viewModel = customersViewModel)
                is Screen.Numbers -> AdminNumbersScreen(viewModel = numbersViewModel)
                is Screen.Operations -> OperationsScreen(viewModel = operationsViewModel)
                is Screen.Settings -> AdminSettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}

private data class NavigationItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)
