package ye.aman.client.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ye.aman.client.data.local.entity.*

@Dao
interface UserDao {
    @Query("SELECT * FROM local_users WHERE id = :userId LIMIT 1")
    fun getUser(userId: String): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM local_users")
    suspend fun clear()
}

@Dao
interface CustomerNumberDao {
    @Query("SELECT * FROM local_customer_numbers ORDER BY addedAt DESC")
    fun getNumbers(): Flow<List<CustomerNumberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNumbers(numbers: List<CustomerNumberEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNumber(number: CustomerNumberEntity)

    @Query("DELETE FROM local_customer_numbers")
    suspend fun clear()
}

@Dao
interface PackageDao {
    @Query("SELECT * FROM local_packages WHERE isActive = 1 AND isVisible = 1 ORDER BY sortOrder ASC")
    fun getPackages(): Flow<List<PackageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackages(packages: List<PackageEntity>)
}

@Dao
interface PaymentMethodDao {
    @Query("SELECT * FROM local_payment_methods WHERE isActive = 1 ORDER BY sortOrder ASC")
    fun getPaymentMethods(): Flow<List<PaymentMethodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethods(methods: List<PaymentMethodEntity>)
}

@Dao
interface ProtectionDao {
    @Query("SELECT * FROM local_protections ORDER BY endAt DESC")
    fun getProtections(): Flow<List<ProtectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProtections(protections: List<ProtectionEntity>)

    @Query("DELETE FROM local_protections")
    suspend fun clear()
}

@Dao
interface ProtectionRequestDao {
    @Query("SELECT * FROM local_protection_requests ORDER BY submittedAt DESC")
    fun getRequests(): Flow<List<ProtectionRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequests(requests: List<ProtectionRequestEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: ProtectionRequestEntity)

    @Query("DELETE FROM local_protection_requests")
    suspend fun clear()
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM local_notifications ORDER BY createdAt DESC")
    fun getNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM local_notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("UPDATE local_notifications SET isRead = 1, readAt = :readAt WHERE id = :id")
    suspend fun markAsRead(id: String, readAt: String)

    @Query("UPDATE local_notifications SET isRead = 1, readAt = :readAt WHERE isRead = 0")
    suspend fun markAllAsRead(readAt: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("DELETE FROM local_notifications")
    suspend fun clear()
}

@Dao
interface SyncMetadataDao {
    @Query("SELECT * FROM local_sync_metadata WHERE entityName = :name")
    suspend fun getMetadata(name: String): SyncMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: SyncMetadataEntity)
}
