package ye.aman.admin.di

import android.content.Context
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import ye.aman.admin.core.network.NetworkMonitor
import ye.aman.admin.core.network.SupabaseConfig
import ye.aman.admin.core.session.SessionManager
import ye.aman.admin.data.local.AmanAdminDatabase
import ye.aman.admin.data.remote.SupabaseAdminRemoteDataSource
import ye.aman.admin.data.repository.*
import ye.aman.admin.domain.usecase.*

object ServiceLocator {
    lateinit var sessionManager: SessionManager
        private set
    lateinit var networkMonitor: NetworkMonitor
        private set
    lateinit var database: AmanAdminDatabase
        private set
    lateinit var remoteDataSource: SupabaseAdminRemoteDataSource
        private set

    // Use cases
    lateinit var adminLoginUseCase: AdminLoginUseCase
        private set
    lateinit var adminResetPasswordUseCase: AdminResetPasswordUseCase
        private set
    lateinit var getDashboardSummaryUseCase: GetAdminDashboardSummaryUseCase
        private set
    lateinit var getCustomersUseCase: GetCustomersUseCase
        private set
    lateinit var updateCustomerStatusUseCase: UpdateCustomerStatusUseCase
        private set
    lateinit var getAdminNumbersUseCase: GetAdminNumbersUseCase
        private set
    lateinit var operationsUseCases: OperationsUseCases
        private set
    lateinit var settingsUseCases: SettingsUseCases
        private set

    fun initialize(context: Context) {
        val appContext = context.applicationContext

        sessionManager = SessionManager(appContext)
        networkMonitor = NetworkMonitor(appContext)
        database = AmanAdminDatabase.getDatabase(appContext)

        val supabaseClient = createSupabaseClient(
            supabaseUrl = SupabaseConfig.SUPABASE_URL,
            supabaseKey = SupabaseConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }

        remoteDataSource = SupabaseAdminRemoteDataSource(supabaseClient)

        // Repositories
        val authRepo = AdminAuthRepositoryImpl(remoteDataSource, sessionManager)
        val dashboardRepo = AdminDashboardRepositoryImpl(remoteDataSource)
        val customerRepo = AdminCustomerRepositoryImpl(remoteDataSource, database)
        val numberRepo = AdminNumberRepositoryImpl(remoteDataSource, database)
        val operationsRepo = AdminOperationsRepositoryImpl(remoteDataSource, database)
        val settingsRepo = AdminSettingsRepositoryImpl(remoteDataSource)

        // Use Cases
        adminLoginUseCase = AdminLoginUseCase(authRepo)
        adminResetPasswordUseCase = AdminResetPasswordUseCase(authRepo)
        getDashboardSummaryUseCase = GetAdminDashboardSummaryUseCase(dashboardRepo)
        getCustomersUseCase = GetCustomersUseCase(customerRepo)
        updateCustomerStatusUseCase = UpdateCustomerStatusUseCase(customerRepo)
        getAdminNumbersUseCase = GetAdminNumbersUseCase(numberRepo)
        operationsUseCases = OperationsUseCases(operationsRepo)
        settingsUseCases = SettingsUseCases(settingsRepo)
    }
}
