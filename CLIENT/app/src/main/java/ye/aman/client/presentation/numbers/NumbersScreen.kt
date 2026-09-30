package ye.aman.client.presentation.numbers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ye.aman.client.domain.model.CustomerNumber
import ye.aman.client.presentation.components.*
import ye.aman.client.presentation.theme.*

@Composable
fun NumbersScreen(
    viewModel: NumbersViewModel,
    onRequestProtection: (CustomerNumber) -> Unit,
    onOpenProtectionDetails: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedNumberDetails by remember { mutableStateOf<CustomerNumber?>(null) }

    Scaffold(
        topBar = {
            AmanTopBar(
                title = "أرقامي",
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة رقم", tint = NavyPrimary)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = NavyPrimary,
                contentColor = SurfaceCard
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إضافة رقم")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .padding(16.dp)
        ) {
            // Search Field
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.updateSearch(it) },
                placeholder = { Text("البحث برقم الهاتف...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs: Protected / Not Protected
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = SurfaceCard,
                contentColor = NavyPrimary
            ) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("مفعلة للحماية", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("غير مفعلة للحماية", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val filteredNumbers = state.numbers.filter { num ->
                val matchesTab = if (state.selectedTab == 0) num.isProtected else !num.isProtected
                val matchesSearch = state.searchQuery.isBlank() || num.normalizedNumber.contains(state.searchQuery)
                matchesTab && matchesSearch
            }

            if (filteredNumbers.isEmpty()) {
                AmanCard {
                    EmptyStateView(
                        title = if (state.selectedTab == 0) "لا توجد أرقام مفعلة للحماية" else "لا توجد أرقام غير مفعلة",
                        subtitle = "يمكنك إضافة أرقام جديدة أو تفعيل الحماية لأرقامك الحالية"
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredNumbers) { item ->
                        NumberItemCard(
                            customerNumber = item,
                            onClick = { selectedNumberDetails = item },
                            onRequestProtection = { onRequestProtection(item) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddNumberDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }

    selectedNumberDetails?.let { num ->
        NumberDetailsDialog(
            customerNumber = num,
            onDismiss = { selectedNumberDetails = null },
            onRequestProtection = {
                selectedNumberDetails = null
                onRequestProtection(num)
            },
            onOpenProtectionDetails = { protId ->
                selectedNumberDetails = null
                onOpenProtectionDetails(protId)
            }
        )
    }
}

@Composable
fun NumberItemCard(
    customerNumber: CustomerNumber,
    onClick: () -> Unit,
    onRequestProtection: () -> Unit
) {
    AmanCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = customerNumber.normalizedNumber,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = customerNumber.providerName,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
                StatusBadge(if (customerNumber.isProtected) "active" else "none")
            }

            if (customerNumber.isProtected) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "الأيام المتبقية: " + customerNumber.daysRemaining + " يوم",
                    style = MaterialTheme.typography.labelMedium.copy(color = TealSecondary, fontWeight = FontWeight.Bold)
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                AmanButton(
                    text = "طلب تفعيل الحماية",
                    onClick = onRequestProtection,
                    modifier = Modifier.fillMaxWidth(),
                    color = TealSecondary
                )
            }
        }
    }
}
