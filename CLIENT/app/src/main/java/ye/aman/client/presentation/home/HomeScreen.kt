package ye.aman.client.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ye.aman.client.domain.model.Protection
import ye.aman.client.presentation.components.*
import ye.aman.client.presentation.theme.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToNumbers: () -> Unit,
    onOpenProtectionDetails: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AmanTopBar(title = "أمان | لوحة التحكم")
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Protection Summary Card
            item {
                AmanCard {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "حالة الحماية العامة",
                                style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary)
                            )
                            StatusBadge(if (state.protectedNumbers > 0) "active" else "expired")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            SummaryStat(label = "إجمالي الأرقام", value = state.totalNumbers.toString())
                            SummaryStat(label = "الأرقام المحمية", value = state.protectedNumbers.toString(), color = GreenSuccess)
                            SummaryStat(label = "غير مفعلة", value = state.notProtectedNumbers.toString(), color = AmberWarning)
                        }
                    }
                }
            }

            // Quick Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AmanButton(
                        text = "إدارة أرقامي",
                        onClick = onNavigateToNumbers,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Active Protections Section
            item {
                Text(
                    text = "الحمايات الفعالة",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )
            }

            if (state.activeProtections.isEmpty()) {
                item {
                    AmanCard {
                        EmptyStateView(
                            title = "لا توجد حمايات نشطة حالياً",
                            subtitle = "أضف رقمك وقم بطلب تفعيل الحماية السنوية لحمايته من السحب"
                        )
                    }
                }
            } else {
                items(state.activeProtections) { prot ->
                    ProtectionCard(
                        protection = prot,
                        onClick = { onOpenProtectionDetails(prot.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryStat(label: String, value: String, color: Color = NavyPrimary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
        )
    }
}

@Composable
fun ProtectionCard(protection: Protection, onClick: () -> Unit) {
    AmanCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = protection.normalizedNumber,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                StatusBadge(protection.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "الشركة: " + protection.providerName,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
            Text(
                text = "الباقة: " + protection.packageNameSnapshot,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "المتبقي: " + protection.daysRemaining + " يوم",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (protection.isNearExpiry) AmberWarning else TealSecondary
                    )
                )
                TextButton(onClick = onClick) {
                    Text("عرض التفاصيل", color = NavyPrimary)
                }
            }
        }
    }
}
