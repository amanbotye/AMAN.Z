package ye.aman.admin.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ye.aman.admin.data.local.entity.*

@Dao
interface CustomerDao {
    @Query("SELECT * FROM admin_customers ORDER BY createdAt DESC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<CustomerEntity>)

    @Query("SELECT * FROM admin_customers WHERE id = :customerId")
    suspend fun getCustomerById(customerId: String): CustomerEntity?

    @Query("UPDATE admin_customers SET status = :status WHERE id = :customerId")
    suspend fun updateStatus(customerId: String, status: String)

    @Query("DELETE FROM admin_customers")
    suspend fun deleteAll()
}

@Dao
interface AdminPhoneNumberDao {
    @Query("SELECT * FROM admin_phone_numbers ORDER BY phoneNumber ASC")
    fun getAllNumbers(): Flow<List<AdminPhoneNumberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(numbers: List<AdminPhoneNumberEntity>)

    @Query("SELECT * FROM admin_phone_numbers WHERE id = :id")
    suspend fun getById(id: String): AdminPhoneNumberEntity?

    @Query("DELETE FROM admin_phone_numbers")
    suspend fun deleteAll()
}

@Dao
interface AdminProtectionRequestDao {
    @Query("SELECT * FROM admin_protection_requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<AdminProtectionRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(requests: List<AdminProtectionRequestEntity>)

    @Query("UPDATE admin_protection_requests SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM admin_protection_requests")
    suspend fun deleteAll()
}

@Dao
interface AdminProtectionDao {
    @Query("SELECT * FROM admin_protections ORDER BY expiresAt ASC")
    fun getAllProtections(): Flow<List<AdminProtectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(protections: List<AdminProtectionEntity>)

    @Query("DELETE FROM admin_protections")
    suspend fun deleteAll()
}

@Dao
interface PaymentTaskDao {
    @Query("SELECT * FROM admin_payment_tasks ORDER BY dueAt ASC")
    fun getAllTasks(): Flow<List<PaymentTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<PaymentTaskEntity>)

    @Query("UPDATE admin_payment_tasks SET status = 'completed', executionResult = :result WHERE id = :id")
    suspend fun markCompleted(id: String, result: String)

    @Query("DELETE FROM admin_payment_tasks")
    suspend fun deleteAll()
}

@Dao
interface AdminSettingsDao {
    @Query("SELECT * FROM admin_telecom_providers ORDER BY sortOrder ASC")
    fun getProviders(): Flow<List<TelecomProviderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProviders(providers: List<TelecomProviderEntity>)

    @Query("SELECT * FROM admin_packages ORDER BY price ASC")
    fun getPackages(): Flow<List<PackageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackages(packages: List<PackageEntity>)

    @Query("SELECT * FROM admin_payment_methods")
    fun getPaymentMethods(): Flow<List<PaymentMethodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethods(methods: List<PaymentMethodEntity>)
}
