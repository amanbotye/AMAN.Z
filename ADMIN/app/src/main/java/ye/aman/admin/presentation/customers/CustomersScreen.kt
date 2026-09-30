package ye.aman.admin.presentation.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import ye.aman.admin.domain.model.CustomerRecord
import ye.aman.admin.presentation.components.AmanAdminTextField
import ye.aman.admin.presentation.components.StatusBadge
import ye.aman.admin.presentation.theme.*

@Composable
fun CustomersScreen(
    viewModel: CustomersViewModel,
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
                    text = "سجل العملاء",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "إدارة حسابات وأرقام وحمايات المشتركين",
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
            label = "البحث في العملاء",
            placeholder = "بحث بالاسم، أو رقم الهاتف، أو البريد..."
        )

        // Customer List
        if (state.isLoading && state.customers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Emerald500)
            }
        } else if (state.filteredCustomers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد سجلات مطابقة للبحث", color = TextMuted, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.filteredCustomers, key = { it.id }) { customer ->
                    CustomerCard(
                        customer = customer,
                        onClick = { viewModel.selectCustomer(customer) }
                    )
                }
            }
        }
    }

    // Customer Details Dialog
    if (state.selectedCustomer != null) {
        CustomerDetailsDialog(
            customer = state.selectedCustomer!!,
            onDismiss = { viewModel.selectCustomer(null) },
            onUpdateStatus = { newStatus ->
                viewModel.updateStatus(state.selectedCustomer!!.id, newStatus)
            }
        )
    }
}

@Composable
fun CustomerCard(
    customer: CustomerRecord,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Slate800, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
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
                    text = customer.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "هاتف: ${customer.phone}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                if (customer.email != null) {
                    Text(
                        text = customer.email,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            StatusBadge(status = customer.status)
        }
    }
}

@Composable
fun CustomerDetailsDialog(
    customer: CustomerRecord,
    onDismiss: () -> Unit,
    onUpdateStatus: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("تفاصيل العميل — ${customer.fullName}", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("معرف العميل: ${customer.id}", fontSize = 11.sp, color = TextMuted)
                Text("رقم التواصل: ${customer.phone}", fontSize = 13.sp, color = TextPrimary)
                Text("البريد: ${customer.email ?: '—'}", fontSize = 13.sp, color = TextPrimary)
                Text("الحالة الحالية: ${customer.status}", fontSize = 13.sp, color = Emerald500)
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (customer.status == "active") {
                    Button(
                        onClick = { onUpdateStatus("suspended") },
                        colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                    ) {
                        Text("تجميد الحساب")
                    }
                } else {
                    Button(
                        onClick = { onUpdateStatus("active") },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                    ) {
                        Text("تفعيل الحساب")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = TextSecondary)
            }
        },
        containerColor = Slate900
    )
}
