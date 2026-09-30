package ye.aman.client.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ye.aman.client.presentation.components.AmanButton
import ye.aman.client.presentation.theme.*

@Composable
fun ResetPasswordDialog(
    viewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("استعادة كلمة المرور", color = NavyPrimary) },
        text = {
            Column {
                Text(
                    text = "أدخل بريدك الإلكتروني المسجل وسنرسل لك تعليمات استعادة كلمة المرور.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("البريد الإلكتروني") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                if (state.resetSent) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "تم إرسال رابط الاستعادة إلى بريدك بنجاح!",
                        color = GreenSuccess,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            AmanButton(
                text = "إرسال",
                onClick = { viewModel.resetPassword(email) },
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
