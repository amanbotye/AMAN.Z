package ye.aman.client.presentation.protection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ye.aman.client.core.common.Constants
import ye.aman.client.domain.model.Protection
import ye.aman.client.presentation.components.AmanButton
import ye.aman.client.presentation.theme.*

@Composable
fun RenewalDialog(
    protection: Protection,
    viewModel: ProtectionViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSubmitted) {
        if (state.isSubmitted) {
            viewModel.resetState()
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("طلب تجديد الحماية", color = NavyPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("تجديد الحماية للرقم: " + protection.normalizedNumber, fontWeight = FontWeight.Bold)
                Text("الأيام المتبقية حالياً: " + protection.daysRemaining + " يوم", color = TealSecondary)
                Text(
                    text = "قاعدة التجديد: تضاف 365 يوماً إلى تاريخ الانتهاء الحالي مباشرة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("اختر وسيلة الدفع:", fontWeight = FontWeight.SemiBold)
                state.paymentMethods.forEach { method ->
                    val isSelected = state.selectedPaymentMethod?.id == method.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.selectPaymentMethod(method) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) NavyPrimary.copy(alpha = 0.1f) else SurfaceCard
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(method.name, fontWeight = FontWeight.Bold)
                            Text("رقم الحساب: " + method.accountNumber, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.paymentReference,
                    onValueChange = { viewModel.updateReference(it) },
                    label = { Text("مرجع التحويل للتجديد") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            AmanButton(
                text = "تأكيد طلب التجديد",
                onClick = { viewModel.submitRenewal(protection.id) },
                isLoading = state.isLoading
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextSecondary)
            }
        }
    )
}
