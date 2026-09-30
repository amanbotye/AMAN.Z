package ye.aman.client.di

import android.content.Context
import ye.aman.client.core.session.SessionManager
import ye.aman.client.data.local.AmanDatabase
import ye.aman.client.data.remote.SupabaseRemoteDataSource
import ye.aman.client.data.repository.*
import ye.aman.client.domain.repository.*
import ye.aman.client.domain.usecase.*

object ServiceLocator {
    private var context: Context? = null

    fun initialize(appContext: Context) {
        context = appContext.applicationContext
    }

    private val sessionManager by lazy { SessionManager(requireContext()) }
    private val database by lazy { AmanDatabase.getInstance(requireContext()) }
    private val remoteDataSource by lazy { SupabaseRemoteDataSource() }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(database.userDao(), sessionManager)
    }

    val customerNumberRepository: CustomerNumberRepository by lazy {
        CustomerNumberRepositoryImpl(database.customerNumberDao(), remoteDataSource)
    }

    val protectionRepository: ProtectionRepository by lazy {
        ProtectionRepositoryImpl(database.protectionDao(), database.protectionRequestDao(), remoteDataSource)
    }

    val packageRepository: PackageRepository by lazy {
        PackageRepositoryImpl(database.packageDao())
    }

    val paymentMethodRepository: PaymentMethodRepository by lazy {
        PaymentMethodRepositoryImpl(database.paymentMethodDao())
    }

    val notificationRepository: NotificationRepository by lazy {
        NotificationRepositoryImpl(database.notificationDao(), remoteDataSource)
    }

    val syncRepository: SyncRepository by lazy {
        SyncRepositoryImpl(customerNumberRepository, protectionRepository, packageRepository, paymentMethodRepository, notificationRepository)
    }

    // Use Cases
    val loginUseCase by lazy { LoginUseCase(authRepository) }
    val registerUseCase by lazy { RegisterUseCase(authRepository) }
    val restoreSessionUseCase by lazy { RestoreSessionUseCase(authRepository) }
    val resetPasswordUseCase by lazy { ResetPasswordUseCase(authRepository) }
    val changePasswordUseCase by lazy { ChangePasswordUseCase(authRepository) }
    val updateAccountUseCase by lazy { UpdateAccountUseCase(authRepository) }
    val logoutUseCase by lazy { LogoutUseCase(authRepository) }

    val loadNumbersUseCase by lazy { LoadCustomerNumbersUseCase(customerNumberRepository) }
    val addNumberUseCase by lazy { AddCustomerNumberUseCase(customerNumberRepository) }

    val loadProtectionsUseCase by lazy { LoadProtectionsUseCase(protectionRepository) }
    val loadRequestsUseCase by lazy { LoadProtectionRequestsUseCase(protectionRepository) }
    val createProtectionRequestUseCase by lazy { CreateProtectionRequestUseCase(protectionRepository) }
    val createRenewalRequestUseCase by lazy { CreateRenewalRequestUseCase(protectionRepository) }

    val loadNotificationsUseCase by lazy { LoadNotificationsUseCase(notificationRepository) }
    val markNotificationReadUseCase by lazy { MarkNotificationReadUseCase(notificationRepository) }
    val markAllNotificationsReadUseCase by lazy { MarkAllNotificationsReadUseCase(notificationRepository) }

    val syncAllDataUseCase by lazy { SyncAllDataUseCase(syncRepository) }

    private fun requireContext(): Context =
        context ?: throw IllegalStateException("ServiceLocator is not initialized with Context")
}
