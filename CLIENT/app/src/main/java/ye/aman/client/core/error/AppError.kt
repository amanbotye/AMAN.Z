package ye.aman.client.core.error

sealed class AppError(open val message: String) {
    data class ValidationError(override val message: String) : AppError(message)
    data class Unauthorized(override val message: String = "انتهت الجلسة أو يجب تسجيل الدخول") : AppError(message)
    data class Forbidden(override val message: String = "غير مصرح لك بتنفيذ هذه العملية") : AppError(message)
    data class NotFound(override val message: String = "العنصر المطلوب غير موجود") : AppError(message)
    data class Duplicate(override val message: String = "البيانات موجودة مسبقاً") : AppError(message)
    data class Conflict(override val message: String = "تعارض في العملية؛ يرجى تحديث البيانات") : AppError(message)
    data class InvalidState(override val message: String = "حالة السجل لا تسمح بتنفيذ العملية") : AppError(message)
    data class Offline(override val message: String = "لا يوجد اتصال بالإنترنت؛ العملية تتطلب اتصالاً موثوقاً") : AppError(message)
    data class ServerError(override val message: String = "خطأ في الخادم؛ يرجى المحاولة لاحقاً") : AppError(message)
    data class Unknown(override val message: String = "حدث خطأ غير متوقع") : AppError(message)

    companion object {
        fun fromThrowable(t: Throwable): AppError {
            val msg = t.message ?: ""
            return when {
                msg.contains("UNAUTHORIZED", ignoreCase = true) -> Unauthorized()
                msg.contains("FORBIDDEN", ignoreCase = true) -> Forbidden()
                msg.contains("DUPLICATE", ignoreCase = true) -> Duplicate(msg)
                msg.contains("CONFLICT", ignoreCase = true) -> Conflict(msg)
                msg.contains("NOT_FOUND", ignoreCase = true) -> NotFound(msg)
                msg.contains("INVALID", ignoreCase = true) -> ValidationError(msg)
                msg.contains("UNSUPPORTED_PREFIX", ignoreCase = true) -> ValidationError("بادئة الرقم غير مدعومة من أي شركة اتصالات نشطة")
                msg.contains("ConnectException", ignoreCase = true) || msg.contains("UnknownHostException", ignoreCase = true) -> Offline()
                else -> ServerError(msg.ifBlank { "خطأ في الاتصال بالخادم" })
            }
        }
    }
}
