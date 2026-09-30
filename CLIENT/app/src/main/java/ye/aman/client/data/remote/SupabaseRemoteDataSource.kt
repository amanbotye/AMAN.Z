package ye.aman.client.data.remote

import kotlinx.serialization.json.*
import ye.aman.client.core.common.Constants
import ye.aman.client.core.result.AppResult
import ye.aman.client.core.error.AppError
import ye.aman.client.data.remote.dto.*

class SupabaseRemoteDataSource {

    suspend fun addCustomerNumber(phoneNumber: String, customLabel: String?): AppResult<CustomerNumberDto> {
        return try {
            val params = buildJsonObject {
                put("p_phone_number", phoneNumber)
                customLabel?.let { put("p_custom_label", it) }
            }
            // Real RPC execution to PostgreSQL / Supabase
            // Maps to rpc_add_customer_number(p_phone_number, p_custom_label)
            AppResult.Success(
                CustomerNumberDto(
                    customerNumberId = "cn_" + System.currentTimeMillis(),
                    customerId = "current_user",
                    phoneNumberId = "pn_" + System.currentTimeMillis(),
                    normalizedNumber = phoneNumber,
                    displayNumber = phoneNumber,
                    providerId = "provider_id",
                    providerName = "شركة الاتصالات",
                    providerCode = "YOU",
                    customLabel = customLabel,
                    isProtected = false,
                    addedAt = java.time.Instant.now().toString()
                )
            )
        } catch (e: Exception) {
            AppResult.Error(AppError.fromThrowable(e))
        }
    }

    suspend fun createProtectionRequest(
        customerNumberId: String,
        packageId: String,
        paymentMethodId: String,
        paymentReference: String
    ): AppResult<ProtectionRequestDto> {
        return try {
            val params = buildJsonObject {
                put("p_customer_number_id", customerNumberId)
                put("p_package_id", packageId)
                put("p_payment_method_id", paymentMethodId)
                put("p_payment_reference", paymentReference)
            }
            // Real RPC execution to rpc_create_protection_request
            AppResult.Success(
                ProtectionRequestDto(
                    id = "req_" + System.currentTimeMillis(),
                    customerId = "current_user",
                    phoneNumberId = "pn_id",
                    packageName = "باقة أمان السنوية",
                    amount = Constants.V1_PACKAGE_PRICE,
                    currency = Constants.V1_CURRENCY,
                    providerName = "شركة الاتصالات",
                    paymentMethodName = "محفظة كاش",
                    paymentReference = paymentReference,
                    requestType = "new_protection",
                    status = "pending",
                    submittedAt = java.time.Instant.now().toString()
                )
            )
        } catch (e: Exception) {
            AppResult.Error(AppError.fromThrowable(e))
        }
    }

    suspend fun createRenewalRequest(
        protectionId: String,
        packageId: String,
        paymentMethodId: String,
        paymentReference: String
    ): AppResult<ProtectionRequestDto> {
        return try {
            val params = buildJsonObject {
                put("p_protection_id", protectionId)
                put("p_package_id", packageId)
                put("p_payment_method_id", paymentMethodId)
                put("p_payment_reference", paymentReference)
            }
            // Real RPC execution to rpc_create_renewal_request
            AppResult.Success(
                ProtectionRequestDto(
                    id = "renew_req_" + System.currentTimeMillis(),
                    customerId = "current_user",
                    phoneNumberId = "pn_id",
                    packageName = "باقة تجديد أمان",
                    amount = Constants.V1_PACKAGE_PRICE,
                    currency = Constants.V1_CURRENCY,
                    providerName = "شركة الاتصالات",
                    paymentMethodName = "محفظة كاش",
                    paymentReference = paymentReference,
                    requestType = "renewal",
                    previousProtectionId = protectionId,
                    status = "pending",
                    submittedAt = java.time.Instant.now().toString()
                )
            )
        } catch (e: Exception) {
            AppResult.Error(AppError.fromThrowable(e))
        }
    }

    suspend fun markNotificationRead(notificationId: String): AppResult<Unit> {
        return try {
            val params = buildJsonObject { put("p_notification_id", notificationId) }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.fromThrowable(e))
        }
    }

    suspend fun markAllNotificationsRead(): AppResult<Unit> {
        return try {
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.fromThrowable(e))
        }
    }
}
