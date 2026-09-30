package ye.aman.client.presentation.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ye.aman.client.domain.model.ClientNotification
import ye.aman.client.presentation.components.*
import ye.aman.client.presentation.theme.*

@Composable
fun NotificationsScreen(viewModel: NotificationsViewModel) {
    val state by viewModel.uiState.collectAsState()
    var selectedNotification by remember { mutableStateOf<ClientNotification?>(null) }

    Scaffold(
        topBar = {
            AmanTopBar(
                title = "الإشعارات",
                actions = {
                    if (state.unreadCount > 0) {
                        TextButton(onClick = { viewModel.markAllRead() }) {
                            Text("قراءة الكل", color = TealSecondary)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .padding(16.dp)
        ) {
            if (state.notifications.isEmpty()) {
                AmanCard {
                    EmptyStateView(
                        title = "لا توجد إشعارات جديدة",
                        subtitle = "ستصلك هنا إشعارات فورية بحالة طلباتك وتحديثات الحماية"
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.notifications) { item ->
                        AmanCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.markRead(item.id)
                                    selectedNotification = item
                                }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.Bold,
                                            color = if (item.isRead) TextPrimary else NavyPrimary
                                        )
                                    )
                                    if (!item.isRead) {
                                        Badge(containerColor = AmberWarning)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.body,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedNotification?.let { notif ->
        AlertDialog(
            onDismissRequest = { selectedNotification = null },
            title = { Text(notif.title, color = NavyPrimary) },
            text = { Text(notif.body, style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                TextButton(onClick = { selectedNotification = null }) {
                    Text("إغلاق")
                }
            }
        )
    }
}
