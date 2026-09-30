package ye.aman.client.core.common

object Constants {
    const val DATABASE_NAME = "aman_client.db"
    const val PREFS_NAME = "aman_client_prefs"

    // V1 Fixed Business Rule
    const val V1_PACKAGE_DURATION_DAYS = 365
    const val V1_PACKAGE_PRICE = 1000.0
    const val V1_CURRENCY = "YER"

    // Status Enums
    const val STATUS_ACTIVE = "active"
    const val STATUS_EXPIRED = "expired"
    const val STATUS_PENDING = "pending"
    const val STATUS_APPROVED = "approved"
    const val STATUS_REJECTED = "rejected"
    const val STATUS_CONFLICT = "conflict"

    // RPC Function Names
    const val RPC_ADD_CUSTOMER_NUMBER = "rpc_add_customer_number"
    const val RPC_CREATE_PROTECTION_REQUEST = "rpc_create_protection_request"
    const val RPC_CREATE_RENEWAL_REQUEST = "rpc_create_renewal_request"
    const val RPC_MARK_NOTIFICATION_READ = "rpc_mark_notification_read"
    const val RPC_MARK_ALL_NOTIFICATIONS_READ = "rpc_mark_all_notifications_read"
}
