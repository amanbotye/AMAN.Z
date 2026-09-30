package ye.aman.client.presentation.protection

import androidx.compose.foundation.background
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
import ye.aman.client.domain.model.CustomerNumber
import ye.aman.client.presentation.components.AmanButton
import ye.aman.client.presentation.theme.*

@Composable
fun RequestProtectionDialog(
    customerNumber: CustomerNumber,
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
        title = { Text("طلب تفعيل الحماية", color = NavyPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "رقم الهاتف: " + customerNumber.normalizedNumber,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "الشركة: " + customerNumber.providerName,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // V1 Package Details (Fixed 1000 YER / 365 Days)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TealSecondary.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("باقة الخدمة المعتمدة (V1)", fontWeight = FontWeight.Bold, color = TealSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("المدة: ${Constants.V1_PACKAGE_DURATION_DAYS} يوماً (سنة كاملة)")
                        Text("الرسوم: ${Constants.V1_PACKAGE_PRICE.toInt()} ${Constants.V1_CURRENCY}")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("اختر وسيلة الدفع للتحويل:", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

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
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(method.name, fontWeight = FontWeight.Bold)
                            Text("المستلم: " + method.recipientName, style = MaterialTheme.typography.bodySmall)
                            Text("رقم الحساب: " + method.accountNumber, style = MaterialTheme.typography.bodySmall, color = NavyPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                state.errorMessage?.let { err ->
                    Text(
                        text = err,
                        color = RedDanger,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RedDanger.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = state.paymentReference,
                    onValueChange = { viewModel.updateReference(it) },
                    label = { Text("مرجع التحويل / رقم الحوالة") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            AmanButton(
                text = "إرسال طلب الحماية",
                onClick = { viewModel.submitNewProtection(customerNumber.id) },
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
