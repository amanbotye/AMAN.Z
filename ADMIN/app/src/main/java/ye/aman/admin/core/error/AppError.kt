package ye.aman.admin.core.error

sealed class AppError(open val message: String) {
    data class NetworkError(override val message: String = "تعذر الاتصال بالخادم، يرجى التحقق من اتصال الإنترنت") : AppError(message)
    data class AuthError(override val message: String = "بيانات الدخول غير صحيحة أو تم إلغاء الصلاحية") : AppError(message)
    data class Forbidden(override val message: String = "عفواً، يتطلب هذا الإجراء صلاحيات المدير الإداري (Admin)") : AppError(message)
    data class SessionExpired(override val message: String = "انتهت صلاحية الجلسة، يرجى إعادة تسجيل الدخول") : AppError(message)
    data class ConflictError(override val message: String = "يوجد تعارض في البيانات أو تم قبول طلب آخر لهذا الرقم") : AppError(message)
    data class ValidationError(override val message: String) : AppError(message)
    data class ServerError(val code: String? = null, override val message: String = "حدث خطأ غير متوقع في الخادم") : AppError(message)
    data class Unknown(override val message: String = "حدث خطأ غير معروف") : AppError(message)
}
