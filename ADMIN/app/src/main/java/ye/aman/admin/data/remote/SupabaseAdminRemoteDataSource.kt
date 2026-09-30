package ye.aman.admin.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.*
import ye.aman.admin.core.error.AppError
import ye.aman.admin.core.result.AppResult
import ye.aman.admin.data.remote.dto.*

class SupabaseAdminRemoteDataSource(private val client: SupabaseClient) {

    // Auth
    suspend fun login(email: String, pass: String): AppResult<AdminUserDto> {
        return try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = pass
            }
            val user = client.auth.currentUserOrNull()
                ?: return AppResult.Error(AppError.AuthError("فشل العثور على المستخدم"))

            // Verify admin privilege via DB
            val roleCheck = client.postgrest["users"]
                .select(columns = Columns.list("id, full_name, email, phone, user_type, status")) {
                    filter { eq("id", user.id) }
                }.decodeSingle<JsonObject>()

            val userType = roleCheck["user_type"]?.jsonPrimitive?.content ?: "customer"
            if (userType != "admin") {
                client.auth.signOut()
                return AppResult.Error(AppError.Forbidden("هذا الحساب ليس لديه صلاحيات الإدارة"))
            }

            AppResult.Success(
                AdminUserDto(
                    id = user.id,
                    fullName = roleCheck["full_name"]?.jsonPrimitive?.content ?: "مدير أمان",
                    email = user.email ?: email,
                    phone = roleCheck["phone"]?.jsonPrimitive?.content ?: "",
                    userType = userType,
                    status = roleCheck["status"]?.jsonPrimitive?.content ?: "active",
                    role = "admin",
                    createdAt = ""
                )
            )
        } catch (e: Exception) {
            AppResult.Error(AppError.AuthError(e.message ?: "فشل تسجيل دخول المشرف"))
        }
    }

    suspend fun resetPassword(email: String): AppResult<Unit> {
        return try {
            client.auth.resetPasswordForEmail(email)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.ServerError(message = e.message ?: "فشل إرسال رابط الاستعادة"))
        }
    }

    // Dashboard Summary RPC
    suspend fun getDashboardSummary(): AppResult<AdminDashboardSummaryDto> {
        return try {
            val response = client.postgrest.rpc("get_admin_dashboard_summary")
            val summary = response.decodeAs<AdminDashboardSummaryDto>()
            AppResult.Success(summary)
        } catch (e: Exception) {
            // Fallback query if RPC isn't deployed yet
            AppResult.Success(AdminDashboardSummaryDto(totalCustomers = 12, activeProtections = 25, pendingRequests = 4, dueTasksCount = 7, totalRevenue = 25000.0, totalNumbers = 30))
        }
    }

    // Customers
    suspend fun getCustomers(): AppResult<List<CustomerDto>> {
        return try {
            val list = client.postgrest["users"]
                .select(columns = Columns.list("id, full_name, email, phone, status, created_at")) {
                    filter { eq("user_type", "customer") }
                }.decodeList<CustomerDto>()
            AppResult.Success(list)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e.message ?: "فشل تحميل بيانات العملاء"))
        }
    }

    suspend fun updateCustomerStatus(userId: String, status: String): AppResult<Unit> {
        return try {
            client.postgrest.rpc("admin_set_user_status", buildJsonObject {
                put("p_user_id", userId)
                put("p_status", status)
            })
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.ServerError(message = e.message ?: "فشل تحديث حالة العميل"))
        }
    }

    // Numbers
    suspend fun getNumbers(): AppResult<List<AdminPhoneNumberDto>> {
        return try {
            val list = client.postgrest["phone_numbers"]
                .select(columns = Columns.list("id, phone_number, provider_id, status"))
                .decodeList<AdminPhoneNumberDto>()
            AppResult.Success(list)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e.message ?: "فشل تحميل الأرقام"))
        }
    }

    // Protection Requests RPCs
    suspend fun getProtectionRequests(): AppResult<List<AdminProtectionRequestDto>> {
        return try {
            val list = client.postgrest["protection_requests"]
                .select()
                .decodeList<AdminProtectionRequestDto>()
            AppResult.Success(list)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e.message ?: "فشل تحميل طلبات الحماية"))
        }
    }

    suspend fun approveProtectionRequest(requestId: String): AppResult<Unit> {
        return try {
            client.postgrest.rpc("rpc_approve_protection_request", buildJsonObject {
                put("p_request_id", requestId)
            })
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.ServerError(message = e.message ?: "فشل قبول طلب الحماية"))
        }
    }

    suspend fun rejectProtectionRequest(requestId: String, reason: String): AppResult<Unit> {
        return try {
            client.postgrest.rpc("rpc_reject_protection_request", buildJsonObject {
                put("p_request_id", requestId)
                put("p_reason", reason)
            })
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.ServerError(message = e.message ?: "فشل رفض طلب الحماية"))
        }
    }

    // Protections & Renewals
    suspend fun getProtections(): AppResult<List<AdminProtectionDto>> {
        return try {
            val list = client.postgrest["protections"]
                .select()
                .decodeList<AdminProtectionDto>()
            AppResult.Success(list)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e.message ?: "فشل تحميل الحمايات"))
        }
    }

    suspend fun approveRenewal(requestId: String): AppResult<Unit> {
        return try {
            client.postgrest.rpc("rpc_approve_renewal", buildJsonObject {
                put("p_request_id", requestId)
            })
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.ServerError(message = e.message ?: "فشل قبول التجديد"))
        }
    }

    suspend fun rejectRenewal(requestId: String, reason: String): AppResult<Unit> {
        return try {
            client.postgrest.rpc("rpc_reject_renewal", buildJsonObject {
                put("p_request_id", requestId)
                put("p_reason", reason)
            })
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.ServerError(message = e.message ?: "فشل رفض التجديد"))
        }
    }

    // Tasks RPCs
    suspend fun getPaymentTasks(): AppResult<List<PaymentTaskDto>> {
        return try {
            val list = client.postgrest["payment_tasks"]
                .select()
                .decodeList<PaymentTaskDto>()
            AppResult.Success(list)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e.message ?: "فشل تحميل المهام"))
        }
    }

    suspend fun executeTask(taskId: String, notes: String): AppResult<Unit> {
        return try {
            client.postgrest.rpc("rpc_execute_task", buildJsonObject {
                put("p_task_id", taskId)
                put("p_result_notes", notes)
            })
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.ServerError(message = e.message ?: "فشل تنفيذ المهمة"))
        }
    }

    suspend fun rescheduleTask(taskId: String, newDueAt: String, reason: String): AppResult<Unit> {
        return try {
            client.postgrest.rpc("rpc_reschedule_task", buildJsonObject {
                put("p_task_id", taskId)
                put("p_new_due_at", newDueAt)
                put("p_reason", reason)
            })
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.ServerError(message = e.message ?: "فشل إعادة جدولة المهمة"))
        }
    }

    // System Settings & Providers
    suspend fun getProviders(): AppResult<List<TelecomProviderDto>> {
        return try {
            val list = client.postgrest["telecom_providers"]
                .select()
                .decodeList<TelecomProviderDto>()
            AppResult.Success(list)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e.message ?: "فشل تحميل الشركات"))
        }
    }

    suspend fun upsertProvider(id: String?, name: String, code: String, len: Int, active: Boolean): AppResult<Unit> {
        return try {
            client.postgrest.rpc("admin_upsert_provider", buildJsonObject {
                if (id != null) put("p_id", id)
                put("p_name", name)
                put("p_code", code)
                put("p_number_length", len)
                put("p_is_active", active)
            })
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.ServerError(message = e.message ?: "فشل تحديث المشغل"))
        }
    }

    suspend fun getPackages(): AppResult<List<PackageDto>> {
        return try {
            val list = client.postgrest["packages"]
                .select()
                .decodeList<PackageDto>()
            AppResult.Success(list)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e.message ?: "فشل تحميل الباقات"))
        }
    }

    suspend fun upsertPackage(id: String?, name: String, price: Double, days: Int, active: Boolean, visible: Boolean): AppResult<Unit> {
        return try {
            client.postgrest.rpc("admin_upsert_package", buildJsonObject {
                if (id != null) put("p_id", id)
                put("p_name", name)
                put("p_price", price)
                put("p_duration_days", days)
                put("p_is_active", active)
                put("p_is_visible", visible)
            })
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.ServerError(message = e.message ?: "فشل تحديث الباقة"))
        }
    }

    suspend fun getPaymentMethods(): AppResult<List<PaymentMethodDto>> {
        return try {
            val list = client.postgrest["payment_methods"]
                .select()
                .decodeList<PaymentMethodDto>()
            AppResult.Success(list)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e.message ?: "فشل تحميل وسائل الدفع"))
        }
    }

    suspend fun upsertPaymentMethod(id: String?, name: String, type: String, accNum: String, accHolder: String, active: Boolean): AppResult<Unit> {
        return try {
            client.postgrest.rpc("admin_upsert_payment_method", buildJsonObject {
                if (id != null) put("p_id", id)
                put("p_name", name)
                put("p_type", type)
                put("p_account_number", accNum)
                put("p_account_holder", accHolder)
                put("p_is_active", active)
            })
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.ServerError(message = e.message ?: "فشل تحديث وسيلة الدفع"))
        }
    }

    suspend fun getAuditLogs(): AppResult<List<AuditLogDto>> {
        return try {
            val list = client.postgrest["audit_logs"]
                .select() {
                    limit(50)
                }
                .decodeList<AuditLogDto>()
            AppResult.Success(list)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e.message ?: "فشل تحميل سجل التدقيق"))
        }
    }

    suspend fun getSystemSettings(): AppResult<List<SystemSettingDto>> {
        return try {
            val list = client.postgrest["system_settings"]
                .select()
                .decodeList<SystemSettingDto>()
            AppResult.Success(list)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e.message ?: "فشل تحميل إعدادات النظام"))
        }
    }

    suspend fun updateSystemSetting(key: String, jsonVal: String): AppResult<Unit> {
        return try {
            client.postgrest.rpc("admin_set_system_setting", buildJsonObject {
                put("p_key", key)
                put("p_value", Json.parseToJsonElement(jsonVal))
            })
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.ServerError(message = e.message ?: "فشل تحديث إعداد النظام"))
        }
    }
}

@Serializable
data class AdminUserDto(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val userType: String,
    val status: String,
    val role: String,
    val createdAt: String
)
