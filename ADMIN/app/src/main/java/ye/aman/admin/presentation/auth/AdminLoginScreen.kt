package ye.aman.admin.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ye.aman.admin.presentation.components.AmanAdminButton
import ye.aman.admin.presentation.components.AmanAdminTextField
import ye.aman.admin.presentation.theme.*

@Composable
fun AdminLoginScreen(
    viewModel: AdminLoginViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Logo Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Emerald500.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "شعار أمان",
                        tint = Emerald500,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Title
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "AMAN | أمان — الإدارة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "لوحة التحكم والتشغيل المركزي للمنظومة",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Email field
                AmanAdminTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    label = "البريد الإلكتروني الإداري",
                    placeholder = "admin@aman.ye"
                )

                // Password field
                AmanAdminTextField(
                    value = state.pass,
                    onValueChange = { viewModel.onPassChange(it) },
                    label = "كلمة المرور الإدارية",
                    placeholder = "••••••••"
                )

                // Error message
                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage ?: "",
                        color = Rose500,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Login button
                AmanAdminButton(
                    text = "تسجيل دخول المشرف",
                    onClick = { viewModel.login(onLoginSuccess) },
                    isLoading = state.isLoading
                )
            }
        }
    }
}
