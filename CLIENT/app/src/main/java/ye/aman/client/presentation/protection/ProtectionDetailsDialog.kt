package ye.aman.client.presentation.protection

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ye.aman.client.domain.model.Protection
import ye.aman.client.presentation.components.AmanButton
import ye.aman.client.presentation.numbers.DetailRow
import ye.aman.client.presentation.theme.*

@Composable
fun ProtectionDetailsDialog(
    protection: Protection,
    onDismiss: () -> Unit,
    onOpenRenewal: (Protection) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تفاصيل الحماية", color = NavyPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailRow("رقم الهاتف", protection.normalizedNumber)
                DetailRow("الشركة", protection.providerName)
                DetailRow("الباقة", protection.packageNameSnapshot)
                DetailRow("تاريخ البدء", protection.startAt)
                DetailRow("تاريخ الانتهاء", protection.endAt)
                DetailRow("الأيام المتبقية", protection.daysRemaining.toString() + " يوم")
                DetailRow("الحالة", if (protection.daysRemaining > 0) "فعالة ومحمية" else "منتهية")
            }
        },
        confirmButton = {
            AmanButton(
                text = "طلب تجديد الحماية",
                onClick = { onOpenRenewal(protection) },
                color = TealSecondary
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = TextSecondary)
            }
        }
    )
}
