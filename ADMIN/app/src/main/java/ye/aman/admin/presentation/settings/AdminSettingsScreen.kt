package ye.aman.admin.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ye.aman.admin.presentation.components.StatusBadge
import ye.aman.admin.presentation.theme.*

@Composable
fun AdminSettingsScreen(
    viewModel: AdminSettingsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "إعدادات النظام والشركات",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "إدارة المشغلين، الباقات السنوية، وسائل الدفع، وسجلات التدقيق",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = { viewModel.loadData() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "تحديث",
                    tint = Emerald500
                )
            }
        }

        // Sub-tabs
        ScrollableTabRow(
            selectedTabIndex = state.selectedTab,
            containerColor = Slate900,
            contentColor = Emerald500,
            edgePadding = 0.dp
        ) {
            listOf("الشركات المشغلة", "باقات الحماية", "وسائل الدفع", "سجل التدقيق (Audit)", "الإعدادات العامة").forEachIndexed { index, title ->
                Tab(
                    selected = state.selectedTab == index,
                    onClick = { viewModel.selectTab(index) },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }

        // Content
        when (state.selectedTab) {
            0 -> ProvidersView(providers = state.providers)
            1 -> PackagesView(packages = state.packages)
            2 -> PaymentMethodsView(methods = state.paymentMethods)
            3 -> AuditLogsView(logs = state.auditLogs)
            4 -> GeneralSettingsView(settings = state.systemSettings)
        }
    }
}

@Composable
fun ProvidersView(providers: List<ye.aman.admin.domain.model.TelecomProvider>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(providers, key = { it.id }) { prov ->
            Card(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp, Slate800, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Slate900)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(prov.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                        Text("الرمز: ${prov.code} • طول الأرقام: ${prov.numberLength}", color = TextSecondary, fontSize = 12.sp)
                    }
                    StatusBadge(status = if (prov.isActive) "active" else "suspended")
                }
            }
        }
    }
}

@Composable
fun PackagesView(packages: List<ye.aman.admin.domain.model.Package>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(packages, key = { it.id }) { pkg ->
            Card(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp, Slate800, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Slate900)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(pkg.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                        Text("السعر: ${pkg.price} ${pkg.currency} • المدة: ${pkg.durationDays} يوم", color = Emerald500, fontSize = 12.sp)
                    }
                    StatusBadge(status = if (pkg.isActive) "active" else "suspended")
                }
            }
        }
    }
}

@Composable
fun PaymentMethodsView(methods: List<ye.aman.admin.domain.model.PaymentMethod>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(methods, key = { it.id }) { m ->
            Card(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp, Slate800, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Slate900)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(m.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                        Text("رقم الحساب: ${m.accountNumber} • المستفيد: ${m.accountHolder}", color = TextSecondary, fontSize = 12.sp)
                    }
                    StatusBadge(status = if (m.isActive) "active" else "suspended")
                }
            }
        }
    }
}

@Composable
fun AuditLogsView(logs: List<ye.aman.admin.domain.model.AuditLog>) {
    if (logs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد سجلات تدقيق حتى الآن", color = TextMuted)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(logs, key = { it.id }) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).border(1.dp, Slate800, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = Slate900)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(log.action, fontWeight = FontWeight.Bold, color = Emerald500, fontSize = 12.sp)
                            Text(log.createdAt, color = TextMuted, fontSize = 10.sp)
                        }
                        Text("الكيان المستهدف: ${log.entityName}", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun GeneralSettingsView(settings: List<ye.aman.admin.domain.model.SystemSetting>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp, Slate800, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Slate900)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("إعدادات الاشتراك والتجديد المركزية", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                    Text("مدة الحماية: 365 يوماً • نافذة التجديد: 30 يوماً قبل الانتهاء", color = TextSecondary, fontSize = 11.sp)
                    Text("العملة الافتراضية: الريال اليمني (YER)", color = Emerald500, fontSize = 11.sp)
                }
            }
        }
    }
}
