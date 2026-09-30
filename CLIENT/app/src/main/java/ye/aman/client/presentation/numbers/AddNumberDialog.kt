package ye.aman.client.presentation.numbers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ye.aman.client.core.validation.PhoneValidator
import ye.aman.client.presentation.components.AmanButton
import ye.aman.client.presentation.theme.*

@Composable
fun AddNumberDialog(
    viewModel: NumbersViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var phoneNumber by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var detectedProvider by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.addSuccess) {
        if (state.addSuccess) {
            viewModel.clearAddSuccess()
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة رقم هاتف جديد", color = NavyPrimary) },
        text = {
            Column {
                Text(
                    text = "أدخل رقم الهاتف وسيتم التعرف تلقائياً على شركة الاتصالات والتحقق من البادئة.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )

                Spacer(modifier = Modifier.height(16.dp))

                (state.errorMessage ?: validationError)?.let { err ->
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
                    value = phoneNumber,
                    onValueChange = { input ->
                        phoneNumber = input
                        val res = PhoneValidator.validateAndDetect(input)
                        detectedProvider = res.detectedProviderCode
                        validationError = res.errorMessage
                    },
                    label = { Text("رقم الهاتف (مثال: 771234567)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                detectedProvider?.let { provider ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "الشركة المكتشفة: $provider",
                        color = TealSecondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("تسمية اختيارية (مثال: رقم العمل)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            AmanButton(
                text = "إضافة الرقم",
                onClick = { viewModel.addNumber(phoneNumber, label) },
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
