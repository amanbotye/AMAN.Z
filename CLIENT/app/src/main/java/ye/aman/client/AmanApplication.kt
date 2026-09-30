package ye.aman.client

import android.app.Application
import ye.aman.client.di.ServiceLocator

class AmanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.initialize(this)
    }
}
