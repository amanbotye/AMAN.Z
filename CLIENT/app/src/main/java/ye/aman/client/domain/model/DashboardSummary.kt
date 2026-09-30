package ye.aman.client.domain.model

data class DashboardSummary(
    val totalNumbers: Int,
    val protectedNumbers: Int,
    val notProtectedNumbers: Int,
    val pendingRequestsCount: Int,
    val activeProtectionsCount: Int,
    val unreadNotificationsCount: Int,
    val activeProtections: List<Protection>
)
