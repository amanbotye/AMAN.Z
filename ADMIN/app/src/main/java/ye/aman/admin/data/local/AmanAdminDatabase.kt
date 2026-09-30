package ye.aman.admin.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ye.aman.admin.core.common.Constants
import ye.aman.admin.data.local.dao.*
import ye.aman.admin.data.local.entity.*

@Database(
    entities = [
        CustomerEntity::class,
        AdminPhoneNumberEntity::class,
        AdminProtectionRequestEntity::class,
        AdminProtectionEntity::class,
        PaymentTaskEntity::class,
        TelecomProviderEntity::class,
        PackageEntity::class,
        PaymentMethodEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AmanAdminDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun phoneNumberDao(): AdminPhoneNumberDao
    abstract fun protectionRequestDao(): AdminProtectionRequestDao
    abstract fun protectionDao(): AdminProtectionDao
    abstract fun paymentTaskDao(): PaymentTaskDao
    abstract fun settingsDao(): AdminSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AmanAdminDatabase? = null

        fun getDatabase(context: Context): AmanAdminDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AmanAdminDatabase::class.java,
                    Constants.DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
