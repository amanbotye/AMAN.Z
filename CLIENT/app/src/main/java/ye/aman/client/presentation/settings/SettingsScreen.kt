package ye.aman.client.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ye.aman.client.presentation.components.AmanCard
import ye.aman.client.presentation.components.AmanTopBar
import ye.aman.client.presentation.theme.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onLoggedOut: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) onLoggedOut()
    }

    Scaffold(
        topBar = { AmanTopBar(title = "الإعدادات") }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            AmanCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("بيانات الحساب", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyPrimary))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("الاسم: " + (state.user?.fullName ?: "-"), style = MaterialTheme.typography.bodyLarge)
                    Text("البريد: " + (state.user?.email ?: "-"), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }

            // Info & Support
            AmanCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("معلومات التطبيق والدعم", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyPrimary))
                    Text(
                        text = "حول أمان (AMAN.Z)",
                        modifier = Modifier.fillMaxWidth().clickable { showAboutDialog = true }
                    )
                    Text(
                        text = "الشروط والأحكام والخصوصية",
                        modifier = Modifier.fillMaxWidth().clickable { showAboutDialog = true }
                    )
                }
            }

            // Logout
            AmanCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    TextButton(
                        onClick = { showLogoutConfirm = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تسجيل الخروج", color = RedDanger, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("تأكيد تسجيل الخروج") },
            text = { Text("هل أنت متأكد من رغبتك في تسجيل الخروج من التطبيق؟") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    viewModel.logout()
                }) {
                    Text("تسجيل الخروج", color = RedDanger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("حول أمان | AMAN", color = NavyPrimary) },
            text = {
                Column {
                    Text("نظام أمان لحماية أرقام الهواتف وإدارة الاشتراكات.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("الإصدار: 1.0.0")
                    Text("حقوق الحماية محفوظة 2026 ©")
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }
}
