package ye.aman.admin

import android.app.Application
import ye.aman.admin.di.ServiceLocator

class AmanAdminApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.initialize(this)
    }
}
