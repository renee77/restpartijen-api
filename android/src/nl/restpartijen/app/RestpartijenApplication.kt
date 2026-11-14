package nl.restpartijen.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

// Starts Koin once for the whole app, before any screen exists.
class RestpartijenApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            // Writes what Koin does to Logcat; handy while learning.
            androidLogger()
            // Gives Koin the app's Context, so modules can use androidContext() (DataStore needs it).
            androidContext(this@RestpartijenApplication)
            modules(appModule)
        }
    }
}
