package ye.aman.client.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ye.aman.client.core.common.Constants
import ye.aman.client.data.local.dao.*
import ye.aman.client.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        CustomerNumberEntity::class,
        PackageEntity::class,
        PaymentMethodEntity::class,
        ProtectionEntity::class,
        ProtectionRequestEntity::class,
        NotificationEntity::class,
        SyncMetadataEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AmanDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun customerNumberDao(): CustomerNumberDao
    abstract fun packageDao(): PackageDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun protectionDao(): ProtectionDao
    abstract fun protectionRequestDao(): ProtectionRequestDao
    abstract fun notificationDao(): NotificationDao
    abstract fun syncMetadataDao(): SyncMetadataDao

    companion object {
        @Volatile
        private var instance: AmanDatabase? = null

        fun getInstance(context: Context): AmanDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AmanDatabase::class.java,
                    Constants.DATABASE_NAME
                ).fallbackToDestructiveMigration()
                .build().also { instance = it }
            }
        }
    }
}
