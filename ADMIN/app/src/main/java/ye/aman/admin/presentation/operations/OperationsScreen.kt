package ye.aman.admin.presentation.operations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ye.aman.admin.domain.model.AdminProtection
import ye.aman.admin.domain.model.AdminProtectionRequest
import ye.aman.admin.domain.model.PaymentTask
import ye.aman.admin.presentation.components.AmanAdminButton
import ye.aman.admin.presentation.components.AmanAdminTextField
import ye.aman.admin.presentation.components.StatusBadge
import ye.aman.admin.presentation.theme.*

@Composable
fun OperationsScreen(
    viewModel: OperationsViewModel,
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
                    text = "التشغيل والعمليات",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "معالجة طلبات الحماية، المهام المستحقة، وتجديد العقود",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = { viewModel.refreshAll() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "تحديث",
                    tint = Emerald500
                )
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = state.selectedTab,
            containerColor = Slate900,
            contentColor = Emerald500,
            indicator = { }
        ) {
            Tab(
                selected = state.selectedTab == 0,
                onClick = { viewModel.selectTab(0) },
                text = { Text("طلبات الحماية (${state.requests.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = state.selectedTab == 1,
                onClick = { viewModel.selectTab(1) },
                text = { Text("المهام المستحقة (${state.tasks.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = state.selectedTab == 2,
                onClick = { viewModel.selectTab(2) },
                text = { Text("عقود الحماية (${state.protections.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        // Tab Content
        when (state.selectedTab) {
            0 -> ProtectionRequestsTab(
                requests = state.requests,
                onApprove = { viewModel.openApproveDialog(it) },
                onReject = { viewModel.openRejectDialog(it) }
            )
            1 -> PaymentTasksTab(
                tasks = state.tasks,
                onExecute = { viewModel.openExecuteTaskDialog(it) },
                onReschedule = { viewModel.openRescheduleTaskDialog(it) }
            )
            2 -> ProtectionsTab(protections = state.protections)
        }
    }

    // Dialogs
    if (state.isApproveDialogOpen && state.selectedRequest != null) {
        ApproveRequestDialog(
            request = state.selectedRequest!!,
            isLoading = state.isLoading,
            onDismiss = { viewModel.dismissDialogs() },
            onConfirm = { viewModel.approveRequest(state.selectedRequest!!.id) }
        )
    }

    if (state.isRejectDialogOpen && state.selectedRequest != null) {
        RejectRequestDialog(
            request = state.selectedRequest!!,
            isLoading = state.isLoading,
            onDismiss = { viewModel.dismissDialogs() },
            onConfirm = { reason -> viewModel.rejectRequest(state.selectedRequest!!.id, reason) }
        )
    }

    if (state.isExecuteTaskDialogOpen && state.selectedTask != null) {
        ExecuteTaskDialog(
            task = state.selectedTask!!,
            isLoading = state.isLoading,
            onDismiss = { viewModel.dismissDialogs() },
            onConfirm = { notes -> viewModel.executeTask(state.selectedTask!!.id, notes) }
        )
    }

    if (state.isRescheduleDialogOpen && state.selectedTask != null) {
        RescheduleTaskDialog(
            task = state.selectedTask!!,
            isLoading = state.isLoading,
            onDismiss = { viewModel.dismissDialogs() },
            onConfirm = { newDue, reason -> viewModel.rescheduleTask(state.selectedTask!!.id, newDue, reason) }
        )
    }
}

@Composable
fun ProtectionRequestsTab(
    requests: List<AdminProtectionRequest>,
    onApprove: (AdminProtectionRequest) -> Unit,
    onReject: (AdminProtectionRequest) -> Unit
) {
    if (requests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد طلبات حماية حالياً", color = TextMuted)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(requests, key = { it.id }) { req ->
                Card(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).border(1.dp, Slate800, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = Slate900)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("طلب رقم: ${req.requestNumber}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                Text("الرقم: ${req.phoneNumber} (${req.providerName})", color = Emerald500, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            StatusBadge(status = req.status)
                        }

                        Text("العميل: ${req.customerName} • سند التحويل: ${req.transferNumber}", color = TextSecondary, fontSize = 11.sp)
                        Text("الباقة: ${req.packageName} (${req.packagePrice} ر.ي) • طريقة الدفع: ${req.paymentMethodName}", color = TextMuted, fontSize = 11.sp)

                        if (req.status == "pending") {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onApprove(req) },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                                ) {
                                    Text("قبول وحسم التعارض", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { onReject(req) },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                                ) {
                                    Text("رفض مع سبب", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentTasksTab(
    tasks: List<PaymentTask>,
    onExecute: (PaymentTask) -> Unit,
    onReschedule: (PaymentTask) -> Unit
) {
    if (tasks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد مهام تشغيلية مستحقة", color = TextMuted)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(tasks, key = { it.id }) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).border(1.dp, Slate800, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = Slate900)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("تنشيط الخط: ${task.phoneNumber}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                Text("المشغل: ${task.providerName} • العميل: ${task.customerName}", color = TextSecondary, fontSize = 11.sp)
                            }
                            StatusBadge(status = task.status)
                        }

                        Text("تاريخ الاستحقاق: ${task.dueAt}", color = Amber400, fontSize = 12.sp, fontWeight = FontWeight.Medium)

                        if (task.status == "due") {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onExecute(task) },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                                ) {
                                    Text("تسجيل التنفيذ (RPC)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { onReschedule(task) },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("إعادة جدولة", fontSize = 11.sp, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProtectionsTab(protections: List<AdminProtection>) {
    if (protections.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد عقود حماية نشطة", color = TextMuted)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(protections, key = { it.id }) { prot ->
                Card(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).border(1.dp, Slate800, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = Slate900)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(prot.phoneNumber, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            StatusBadge(status = prot.status)
                        }
                        Text("العميل: ${prot.customerName} • المشغل: ${prot.providerName}", color = TextSecondary, fontSize = 12.sp)
                        Text("تاريخ الانتهاء: ${prot.expiresAt} (متبقي ${prot.remainingDays} يوم)", color = Emerald500, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ApproveRequestDialog(
    request: AdminProtectionRequest,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تأكيد قبول طلب الحماية ذرياً", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("هل تريد بالتأكيد قبول الطلب رقم ${request.requestNumber} للرقم ${request.phoneNumber}؟", fontSize = 13.sp)
                Text("وفق عقد AMAN_DATABASE.sql، سيتم:", color = Emerald500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("1. إنشاء عقد حماية جديد لمدة 365 يوماً.\n2. توليد خطة المهام التشغيلية الأولى.\n3. حسم أي تعارض برفض الطلبات المعلقة الأخرى للرقم نفسه آلياً.", color = TextMuted, fontSize = 11.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
            ) {
                Text(if (isLoading) "جارٍ القبول..." else "قبول وتفعيل الحماية")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = TextSecondary) }
        },
        containerColor = Slate900
    )
}

@Composable
fun RejectRequestDialog(
    request: AdminProtectionRequest,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("رفض طلب الحماية", fontWeight = FontWeight.Bold, color = Rose500) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("يرجى إدخال سبب الرفض الإلزامي للطلب رقم ${request.requestNumber}:", fontSize = 13.sp)
                AmanAdminTextField(
                    value = reason,
                    onValueChange = { reason = it; isError = false },
                    label = "سبب الرفض الإلزامي",
                    placeholder = "مثال: سند التحويل غير واضح، المبلغ ناقص...",
                    isError = isError,
                    errorMessage = "يجب كتابة سبب الرفض"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isBlank()) {
                        isError = true
                    } else {
                        onConfirm(reason)
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Rose500)
            ) {
                Text(if (isLoading) "جارٍ الرفض..." else "تأكيد الرفض")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = TextSecondary) }
        },
        containerColor = Slate900
    )
}

@Composable
fun ExecuteTaskDialog(
    task: PaymentTask,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var notes by remember { mutableStateOf("تم التسديد والتنشيط بنجاح لدى المشغل") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسجيل تنفيذ المهمة التشغيلية", fontWeight = FontWeight.Bold, color = Emerald500) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("تأكيد شحن/تنشيط الخط للرقم: ${task.phoneNumber}", fontSize = 13.sp)
                AmanAdminTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "ملاحظات التنفيذ الفني",
                    placeholder = "أدخل رقم عملية السداد لدى المشغل..."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(notes) },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
            ) {
                Text(if (isLoading) "جارٍ التنفيذ..." else "حفظ وإعادة الجدولة الآلية")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = TextSecondary) }
        },
        containerColor = Slate900
    )
}

@Composable
fun RescheduleTaskDialog(
    task: PaymentTask,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var newDue by remember { mutableStateOf("2026-10-15T00:00:00Z") }
    var reason by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إعادة جدولة موعد المهمة", fontWeight = FontWeight.Bold, color = Amber400) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("التاريخ الأصلي للمهمة: ${task.dueAt}", fontSize = 12.sp, color = TextMuted)
                AmanAdminTextField(
                    value = newDue,
                    onValueChange = { newDue = it },
                    label = "تاريخ الاستحقاق الجديد (ISO-8601)",
                    placeholder = "YYYY-MM-DD..."
                )
                AmanAdminTextField(
                    value = reason,
                    onValueChange = { reason = it; isError = false },
                    label = "سبب إعادة الجدولة الإلزامي",
                    placeholder = "مثال: عطل مؤقت في نظام المشغل...",
                    isError = isError,
                    errorMessage = "سبب التأجيل إلزامي"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isBlank()) {
                        isError = true
                    } else {
                        onConfirm(newDue, reason)
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Amber500)
            ) {
                Text(if (isLoading) "جارٍ الحفظ..." else "حفظ الموعد الجديد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = TextSecondary) }
        },
        containerColor = Slate900
    )
}
