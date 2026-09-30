package ye.aman.admin.presentation.numbers

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
import ye.aman.admin.domain.model.AdminPhoneNumber
import ye.aman.admin.presentation.components.AmanAdminTextField
import ye.aman.admin.presentation.components.StatusBadge
import ye.aman.admin.presentation.theme.*

@Composable
fun AdminNumbersScreen(
    viewModel: AdminNumbersViewModel,
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
                    text = "سجل الأرقام الوطنية",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "متابعة أرقام الاتصالات وحالة الحماية والمشغلين",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = { viewModel.refresh() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "تحديث",
                    tint = Emerald500
                )
            }
        }

        // Search
        AmanAdminTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.onSearchChange(it) },
            label = "البحث في الأرقام",
            placeholder = "بحث برقم الهاتف أو المشغل..."
        )

        // Filter Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "all" to "كل الأرقام",
                "protected" to "المحمية",
                "unprotected" to "غير المحمية"
            ).forEach { (key, label) ->
                FilterChip(
                    selected = state.filterType == key,
                    onClick = { viewModel.onFilterChange(key) },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Emerald500,
                        selectedLabelColor = Slate950,
                        containerColor = Slate900,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        // List
        if (state.isLoading && state.numbers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Emerald500)
            }
        } else if (state.filteredNumbers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد أرقام مطابقة للتصفية", color = TextMuted, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.filteredNumbers, key = { it.id }) { item ->
                    AdminNumberCard(number = item)
                }
            }
        }
    }
}

@Composable
fun AdminNumberCard(number: AdminPhoneNumber) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Slate800, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Slate900)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = number.phoneNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${number.providerName} • ${if (number.isProtected) "محمي" else "غير محمي"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                if (number.customerName != null) {
                    Text(
                        text = "المالك: ${number.customerName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            StatusBadge(status = if (number.isProtected) "active" else "unassigned")
        }
    }
}
