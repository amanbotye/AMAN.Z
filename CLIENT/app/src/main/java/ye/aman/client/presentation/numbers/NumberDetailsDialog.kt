package ye.aman.client.presentation.numbers

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ye.aman.client.domain.model.CustomerNumber
import ye.aman.client.presentation.components.AmanButton
import ye.aman.client.presentation.components.StatusBadge
import ye.aman.client.presentation.theme.*

@Composable
fun NumberDetailsDialog(
    customerNumber: CustomerNumber,
    onDismiss: () -> Unit,
    onRequestProtection: () -> Unit,
    onOpenProtectionDetails: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تفاصيل الرقم",
                color = NavyPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailRow("رقم الهاتف", customerNumber.normalizedNumber)
                DetailRow("شركة الاتصالات", customerNumber.providerName)
                DetailRow("حالة الحماية", if (customerNumber.isProtected) "مفعلة للحماية" else "غير مفعلة")
                if (customerNumber.customLabel != null) {
                    DetailRow("التسمية", customerNumber.customLabel)
                }

                if (customerNumber.isProtected) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    DetailRow("تاريخ الانتهاء", customerNumber.protectionEndAt ?: "-")
                    DetailRow("الأيام المتبقية", customerNumber.daysRemaining.toString() + " يوم")
                }
            }
        },
        confirmButton = {
            if (customerNumber.isProtected && customerNumber.protectionId != null) {
                AmanButton(
                    text = "عرض تفاصيل الحماية",
                    onClick = { onOpenProtectionDetails(customerNumber.protectionId) }
                )
            } else {
                AmanButton(
                    text = "طلب تفعيل الحماية",
                    onClick = onRequestProtection,
                    color = TealSecondary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = TextSecondary)
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, color = TextPrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}
