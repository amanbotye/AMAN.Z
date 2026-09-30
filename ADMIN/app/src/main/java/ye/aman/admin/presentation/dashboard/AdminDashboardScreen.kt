package ye.aman.admin.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ye.aman.admin.presentation.components.MetricCard
import ye.aman.admin.presentation.theme.*

@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel,
    onNavigateToOperations: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "لوحة التحكم التشغيلية",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "ملخص مؤشرات الأداء الحية لمنظومة أمان",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = { viewModel.loadSummary() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "تحديث",
                    tint = Emerald500
                )
            }
        }

        // Metrics Grid (2x2)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "الحمايات النشطة",
                value = "${state.summary.activeProtections}",
                subtitle = "من إجمالي ${state.summary.totalNumbers} رقم موثق",
                valueColor = Emerald500,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "طلبات الحماية المعلقة",
                value = "${state.summary.pendingRequests}",
                subtitle = "تتطلب التحقق والقبول",
                valueColor = Amber400,
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "المهام المستحقة للتنفيذ",
                value = "${state.summary.dueTasksCount}",
                subtitle = "تنشيط وتسديد لدى المشغلين",
                valueColor = Rose500,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "إجمالي العملاء",
                value = "${state.summary.totalCustomers}",
                subtitle = "حسابات مسجلة وموثقة",
                valueColor = Teal400,
                modifier = Modifier.weight(1f)
            )
        }

        // Revenue Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "إجمالي الإيرادات ورسوم الحماية",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "${state.summary.totalRevenue} ر.ي",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "مستندة إلى قيود financial_transactions وقاعدة البيانات المعتمدة",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // Quick Actions
        Text(
            text = "إجراءات الوصول السريع",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onNavigateToOperations,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
            ) {
                Text("معالجة الطلبات والمهام", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onNavigateToCustomers,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("عرض سجل العملاء", fontSize = 12.sp, color = TextPrimary)
            }
        }
    }
}
